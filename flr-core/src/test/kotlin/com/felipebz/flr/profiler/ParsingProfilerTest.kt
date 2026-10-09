/**
 * FLR
 * Copyright (C) 2021-2026 Felipe Zorzo
 * mailto:felipe AT felipezorzo DOT com DOT br
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.felipebz.flr.profiler

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.grammar.ContextKey
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.matchers.ParseNode
import com.felipebz.flr.internal.vm.*
import com.felipebz.flr.parser.ParseRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParsingProfilerTest {
    private val key = ContextKey<Boolean>()

    private fun rule(name: String, memoized: Boolean = true) =
        RuleDefinition(name).apply { if (memoized) enableMemoization() }

    private fun seq(vararg e: ParsingExpression) = SequenceExpression(*e)
    private fun alt(vararg e: ParsingExpression) = FirstOfExpression(*e)
    private fun s(x: String) = StringExpression(x)
    private fun ctx(e: ParsingExpression) = ContextExpression(key, true, true, e)

    /** hits, empty, matcher, context, matches, overwritesOther */
    private fun TargetProfile.raw() =
        listOf(memoHits, memoEmptyMisses, memoMatcherMisses, memoContextMisses, matches, memoOverwritesOther)

    /** lookups, executed invocations, failures, stores */
    private fun TargetProfile.derived() = listOf(memoLookups, executedInvocations, failures, memoStores)

    private fun ProgramProfile.target(name: String) = targets.single { it.name == name }

    private fun shape(n: ParseNode): String =
        "${n.matcher}[${n.startIndex},${n.endIndex}](${n.children.joinToString(",") { shape(it) }})"

    /** Parses [input] with an unprofiled and a profiled run and requires the same outcome and tree. */
    private fun profile(
        root: RuleDefinition,
        input: String,
        contextual: Boolean,
        profiler: ParsingProfiler = ParsingProfiler()
    ): ProgramProfile {
        if (contextual) root.expression = ctx(root.expression!!)
        val ordinary = Machine.parse(input.toCharArray(), MutableGrammarCompiler.compile(root))
        val profiled = ParseRunner(root).parse(input.toCharArray(), profiler)
        assertThat(profiled.isMatched()).isEqualTo(ordinary.isMatched())
        if (ordinary.isMatched()) {
            assertThat(shape(profiled.getParseTreeRoot())).isEqualTo(shape(ordinary.getParseTreeRoot()))
        }
        return profiler.snapshot().programs.single()
    }

    @Test fun memoHitAfterBacktrackingRetry() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val root = rule("ROOT", false).apply { expression = alt(seq(a, s("x")), seq(a, s("y"))) }
            val p = profile(root, "ay", contextual)
            // first call: empty slot, executed, matched, stored; retry after "x" fails: hit
            assertThat(p.target("A").raw()).containsExactly(1L, 1L, 0L, 0L, 1L, 0L)
            assertThat(p.target("A").derived()).containsExactly(2L, 1L, 0L, 1L)
            assertThat(p.target("A").memoHitRate).isEqualTo(0.5)
            assertThat(p.target("A").callSites).isEqualTo(2)
            assertThat(p.target("A").kind).isEqualTo(TargetKind.CALLED_RULE)
        }
    }

    @Test fun emptyMissWithNoReuse() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val root = rule("ROOT", false).apply { expression = seq(a, s("b")) }
            val p = profile(root, "ab", contextual)
            assertThat(p.target("A").raw()).containsExactly(0L, 1L, 0L, 0L, 1L, 0L)
            assertThat(p.target("A").derived()).containsExactly(1L, 1L, 0L, 1L)
        }
    }

    @Test fun matcherMissAndOverwriteOfAnotherMatchersMemo() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val b = rule("B").apply { expression = seq(a, s("b")) }
            val root = rule("ROOT", false).apply { expression = alt(seq(b, s("x")), seq(a, s("b"), s("c"))) }
            val p = profile(root, "abc", contextual)
            // A stores into an empty slot (no overwrite); B then replaces A's memo; the retry of A finds B's memo
            // (matcher miss) and replaces it again.
            assertThat(p.target("A").raw()).containsExactly(0L, 1L, 1L, 0L, 2L, 1L)
            assertThat(p.target("A").derived()).containsExactly(2L, 2L, 0L, 2L)
            assertThat(p.target("B").raw()).containsExactly(0L, 1L, 0L, 0L, 1L, 1L)
        }
    }

    @Test fun contextCompatibleHitUsesStructuralContextEquality() {
        val a = rule("A").apply { expression = s("a") }
        val root = rule("ROOT", false).apply { expression = alt(seq(ctx(seq(a, s("x")))), seq(ctx(seq(a, s("y"))))) }
        val p = profile(root, "ay", false)
        // two separately entered scopes build equal contexts
        assertThat(p.target("A").raw()).containsExactly(1L, 1L, 0L, 0L, 1L, 0L)
    }

    @Test fun contextMissOnSameMatcher() {
        val a = rule("A").apply { expression = s("a") }
        val root = rule("ROOT", false).apply { expression = alt(seq(ctx(a), s("x")), seq(a, s("y"))) }
        val p = profile(root, "ay", false)
        // second call has no context, the memo was stored under one; same matcher so no "other" overwrite
        assertThat(p.target("A").raw()).containsExactly(0L, 1L, 0L, 1L, 2L, 0L)
        assertThat(p.target("A").derived()).containsExactly(2L, 2L, 0L, 2L)
    }

    @Test fun firstContextActivationDiscardsEarlierMemos() {
        val a = rule("A").apply { expression = s("a") }
        val root = rule("ROOT", false).apply {
            expression = alt(seq(a, ctx(s("b")), s("x")), seq(a, s("b"), s("y")))
        }
        val p = profile(root, "aby", false)
        assertThat(p.target("A").raw()).containsExactly(0L, 2L, 0L, 0L, 2L, 0L)
    }

    @Test fun nonMemoizingTargetLooksUpButNeverStoresOrHits() {
        for (contextual in listOf(false, true)) {
            val r = rule("R", false).apply { expression = s("a") }
            val root = rule("ROOT", false).apply { expression = alt(seq(r, s("x")), seq(r, s("y"))) }
            val p = profile(root, "ay", contextual)
            assertThat(p.target("R").raw()).containsExactly(0L, 2L, 0L, 0L, 2L, 0L)
            assertThat(p.target("R").derived()).containsExactly(2L, 2L, 0L, 0L)
            assertThat(p.target("R").memoizing).isFalse()
        }
        for (contextual in listOf(false, true)) {
            val r = rule("R", false).apply { expression = s("a") }
            val b = rule("B").apply { expression = seq(r, s("b")) }
            val root = rule("ROOT", false).apply { expression = alt(seq(b, s("x")), seq(r, s("b"), s("c"))) }
            val p = profile(root, "abc", contextual)
            // B's memo occupies the slot R looks up; R never stores, so R never overwrites
            assertThat(p.target("R").raw()).containsExactly(0L, 1L, 1L, 0L, 2L, 0L)
            assertThat(p.target("R").memoStores).isZero()
            assertThat(p.target("B").raw()).containsExactly(0L, 1L, 0L, 0L, 1L, 0L)
        }
    }

    @Test fun failingInvocationsAreNotMemoizedAndRetried() {
        for (contextual in listOf(false, true)) {
            val f = rule("F").apply { expression = seq(s("a"), s("z")) }
            val root = rule("ROOT", false).apply {
                expression = alt(seq(f, s("x")), seq(f, s("y")), seq(s("a"), s("b")))
            }
            val p = profile(root, "ab", contextual)
            assertThat(p.target("F").raw()).containsExactly(0L, 2L, 0L, 0L, 0L, 0L)
            assertThat(p.target("F").derived()).containsExactly(2L, 2L, 2L, 0L)
        }
    }

    @Test fun callSitesOfOneTargetShareOneProfile() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val root = rule("ROOT", false).apply { expression = seq(a, a) }
            val p = profile(root, "aa", contextual)
            assertThat(p.callTargetCount).isEqualTo(1)
            assertThat(p.target("A").callSites).isEqualTo(2)
            assertThat(p.target("A").raw()).containsExactly(0L, 2L, 0L, 0L, 2L, 0L)
        }
    }

    @Test fun reusedMatcherKeepsDistinctTargets() {
        for (contextual in listOf(false, true)) {
            val t = TokenExpression(GenericTokenType.IDENTIFIER, s("a"))
            val root = rule("ROOT", false).apply { expression = seq(t, t) }
            val p = profile(root, "aa", contextual)
            assertThat(p.callTargetCount).isEqualTo(2)
            assertThat(p.targets.map { it.kind }).containsOnly(TargetKind.CALLED_ANONYMOUS)
            assertThat(p.targets.map { it.entryAddress }.distinct()).hasSize(2)
            assertThat(p.targets.map { it.name }.distinct()).hasSize(2)
            for (target in p.targets) {
                assertThat(target.raw()).containsExactly(0L, 1L, 0L, 0L, 1L, 0L)
                assertThat(target.memoizing).isFalse()
                assertThat(target.callSites).isEqualTo(1)
            }
        }
    }

    @Test fun anonymousTargetLookupSeesOtherMatchersMemoButNeverStores() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val t = TokenExpression(GenericTokenType.IDENTIFIER, s("a"))
            val root = rule("ROOT", false).apply { expression = alt(seq(a, s("x")), seq(t, s("y"))) }
            val p = profile(root, "ay", contextual)
            val anonymous = p.targets.single { it.kind == TargetKind.CALLED_ANONYMOUS }
            assertThat(anonymous.raw()).containsExactly(0L, 0L, 1L, 0L, 1L, 0L)
            assertThat(anonymous.memoStores).isZero()
        }
    }

    @Test fun initialRootIsSeparateFromCalledTargets() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val ok = profile(rule("ROOT", true).apply { expression = seq(a, s("b")) }, "ab", contextual)
            assertThat(ok.parses).isEqualTo(1)
            assertThat(ok.root.kind).isEqualTo(TargetKind.INITIAL_ROOT)
            assertThat(ok.root.targetId).isEqualTo(-1)
            assertThat(ok.root.memoLookups).isZero()
            // the root completes last at index 0, replacing the memo of its leftmost child A
            assertThat(ok.root.raw()).containsExactly(0L, 0L, 0L, 0L, 1L, 1L)
            assertThat(ok.root.derived()).containsExactly(0L, 1L, 0L, 1L)
            assertThat(ok.callTargetCount).isEqualTo(1)

            val bad = profile(rule("ROOT", true).apply { expression = seq(a, s("b")) }, "ac", contextual)
            assertThat(bad.root.raw()).containsExactly(0L, 0L, 0L, 0L, 0L, 0L)
            assertThat(bad.root.derived()).containsExactly(0L, 1L, 1L, 0L)
        }
    }

    @Test fun recursiveRootKeepsItsCalledTargetSeparate() {
        for (contextual in listOf(false, true)) {
            val r = rule("R", true)
            r.expression = seq(s("a"), OptionalExpression(r))
            val p = profile(r, "aa", contextual)
            assertThat(p.callTargetCount).isEqualTo(1)
            // called form: calls at index 1 (matches) and 2 (fails)
            assertThat(p.targets.single().raw()).containsExactly(0L, 2L, 0L, 0L, 1L, 0L)
            assertThat(p.targets.single().derived()).containsExactly(2L, 2L, 1L, 1L)
            // initial invocation: no lookup, one match, one store
            assertThat(p.root.raw()).containsExactly(0L, 0L, 0L, 0L, 1L, 0L)
            assertThat(p.root.derived()).containsExactly(0L, 1L, 0L, 1L)
        }
    }

    @Test fun rootStartsAccumulateOverParses() {
        val a = rule("A").apply { expression = s("a") }
        val root = rule("ROOT", false).apply { expression = seq(a, s("b")) }
        val runner = ParseRunner(root)
        val profiler = ParsingProfiler()
        runner.parse("ab".toCharArray(), profiler)
        runner.parse("ab".toCharArray(), profiler)
        runner.parse("ac".toCharArray(), profiler)
        val p = profiler.snapshot().programs.single()
        assertThat(p.parses).isEqualTo(3)
        assertThat(p.root.derived()).containsExactly(0L, 3L, 1L, 0L)
        assertThat(p.target("A").raw()).containsExactly(0L, 3L, 0L, 0L, 3L, 0L)
    }

    @Test fun programsAreProfiledSeparately() {
        val profiler = ParsingProfiler()
        val a = rule("A").apply { expression = s("a") }
        val firstRoot = rule("ROOT1", false).apply { expression = seq(a, a) }
        val secondRoot = rule("ROOT2", false).apply { expression = seq(a, s("b")) }
        val first = ParseRunner(firstRoot)
        val second = ParseRunner(secondRoot)
        first.parse("aa".toCharArray(), profiler)
        second.parse("ab".toCharArray(), profiler)
        first.parse("aa".toCharArray(), profiler)
        // the same grammar compiled again is another program instance
        ParseRunner(firstRoot).parse("aa".toCharArray(), profiler)

        val programs = profiler.snapshot().programs
        assertThat(programs.map { it.programId }).containsExactly(0, 1, 2)
        assertThat(programs.map { it.root.name }).containsExactly("ROOT1", "ROOT2", "ROOT1")
        assertThat(programs.map { it.parses }).containsExactly(2L, 1L, 1L)
        // both programs have a target 0, named A; their counters must not mix
        assertThat(programs.map { it.targets.single().targetId }).containsOnly(0)
        assertThat(programs.map { it.targets.single().matches }).containsExactly(4L, 1L, 2L)
    }

    @Test fun snapshotDoesNotChangeWhenParsingContinues() {
        val a = rule("A").apply { expression = s("a") }
        val runner = ParseRunner(rule("ROOT", false).apply { expression = seq(a, s("b")) })
        val profiler = ParsingProfiler()
        runner.parse("ab".toCharArray(), profiler)
        val before = profiler.snapshot()
        runner.parse("ab".toCharArray(), profiler)
        assertThat(before.programs.single().target("A").matches).isEqualTo(1)
        assertThat(before.programs.single().parses).isEqualTo(1)
        assertThat(profiler.snapshot().programs.single().target("A").matches).isEqualTo(2)
    }

    @Test fun resetZeroesCountersButKeepsPrograms() {
        val a = rule("A").apply { expression = s("a") }
        val runner = ParseRunner(rule("ROOT", false).apply { expression = seq(a, s("b")) })
        val profiler = ParsingProfiler()
        runner.parse("ab".toCharArray(), profiler)
        profiler.reset()
        val cleared = profiler.snapshot().programs.single()
        assertThat(cleared.parses).isZero()
        assertThat(cleared.root.derived()).containsExactly(0L, 0L, 0L, 0L)
        assertThat(cleared.target("A").raw()).containsOnly(0L)
        assertThat(cleared.target("A").callSites).isEqualTo(1)
        runner.parse("ab".toCharArray(), profiler)
        val again = profiler.snapshot().programs.single()
        assertThat(again.programId).isEqualTo(cleared.programId)
        assertThat(again.target("A").raw()).containsExactly(0L, 1L, 0L, 0L, 1L, 0L)
    }

    @Test fun unprofiledParsesUseOrdinaryMachinesAndProfiledParsesUseProfilingMachines() {
        for (contextual in listOf(false, true)) {
            val a = rule("A").apply { expression = s("a") }
            val body: ParsingExpression = seq(a, s("b"))
            val root = rule("ROOT", false).apply { expression = if (contextual) ctx(body) else body }
            val grammar = MutableGrammarCompiler.compile(root)
            val counters = ParsingProfiler().countersFor(grammar)

            val ordinary = Machine.createMachine(charArrayOf(), emptyArray(), grammar) { }
            val profiling = Machine.createMachine(charArrayOf(), emptyArray(), grammar, { }, counters)
            if (contextual) {
                assertThat(ordinary).isExactlyInstanceOf(ContextAwareMachine::class.java)
                assertThat(profiling).isExactlyInstanceOf(ProfilingContextAwareMachine::class.java)
            } else {
                assertThat(ordinary).isExactlyInstanceOf(Machine::class.java)
                assertThat(profiling).isExactlyInstanceOf(ProfilingMachine::class.java)
            }
            assertThat(ProfilingMachine::class.java.superclass).isEqualTo(Machine::class.java)
            assertThat(ProfilingContextAwareMachine::class.java.superclass).isEqualTo(ContextAwareMachine::class.java)
        }
    }

    @Test fun targetsReachedThroughDifferentMatchersAreFlaggedAmbiguous() {
        val first = rule("FIRST").apply { expression = s("a") }
        val second = rule("SECOND", false).apply { expression = s("a") }
        // two call sites, different matcher objects, one entry address (2)
        val program = InstructionProgram.link(
            arrayOf(Instruction.call(2, first), Instruction.call(1, second), Instruction.ret())
        )
        val grammar = CompiledGrammar(program, mapOf(first.ruleKey to first), first.ruleKey, 0)
        val profile = ProgramCounters(grammar, 0).toProfile()
        val target = profile.targets.single()
        assertThat(profile.callTargetCount).isEqualTo(1)
        assertThat(target.callSites).isEqualTo(2)
        assertThat(target.ambiguousMatchers).isTrue()
        assertThat(target.name).isEqualTo("FIRST | SECOND")
        assertThat(target.memoizing).isFalse()
    }

    @Test fun counterStorageIsPerProgramNotPerParseOrInputLength() {
        val a = rule("A").apply { expression = s("a") }
        val b = rule("B").apply { expression = seq(a, s("b")) }
        val root = rule("ROOT", false).apply {
            expression = seq(OneOrMoreExpression(alt(seq(b, s("x")), seq(a, s("b"), s("c")))))
        }
        val grammar = MutableGrammarCompiler.compile(root)
        val profiler = ParsingProfiler()
        val counters = profiler.countersFor(grammar)
        val array = counters.counters
        // callTargetCount target slots plus one root slot
        assertThat(array.size).isEqualTo((grammar.callTargetCount + 1) * ProgramCounters.STRIDE)
        for (repetitions in listOf(1, 10, 1000)) {
            Machine.parse("abc".repeat(repetitions).toCharArray(), grammar, counters)
            assertThat(profiler.countersFor(grammar).counters).isSameAs(array)
            assertThat(array.size).isEqualTo((grammar.callTargetCount + 1) * ProgramCounters.STRIDE)
        }
        assertThat(profiler.snapshot().programs).hasSize(1)
        assertThat(profiler.snapshot().programs.single().parses).isEqualTo(3)
    }
}
