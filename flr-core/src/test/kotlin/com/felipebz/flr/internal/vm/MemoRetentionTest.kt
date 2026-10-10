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
package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.api.Token
import com.felipebz.flr.grammar.ContextKey
import com.felipebz.flr.grammar.GrammarBuilder
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.matchers.ParseNode
import com.felipebz.flr.parser.ParseRunner
import com.felipebz.flr.profiler.ParsingProfiler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Every grammar is built twice from fresh rules, without and with retention, and both parses must agree on the tree,
 * the error index and any exception. Rule executions are observed through [Entries] placed first in each rule body.
 */
class MemoRetentionTest {
    private val key = ContextKey<Int>()
    private val scopeKey = ContextKey<Boolean>()

    /** PLAIN: ordinary VM; INACTIVE: context-aware VM that never activates context; ACTIVE: context active from the start. */
    private enum class Mode { PLAIN, INACTIVE, ACTIVE }

    private class Grammar(private val retained: Set<String>) {
        val entries = mutableMapOf<String, Entries>()
        val rules = mutableMapOf<String, RuleDefinition>()

        fun rule(name: String): RuleDefinition = rules.getOrPut(name) {
            RuleDefinition(name).apply { if (name in retained) enableMemoRetention() else enableMemoization() }
        }

        fun define(name: String, vararg e: ParsingExpression): RuleDefinition = rule(name).apply {
            expression = SequenceExpression(entries.getOrPut(name) { Entries() }, *e)
        }

        fun runs(name: String): List<Int> = entries[name]?.positions ?: emptyList()
    }

    private class Run(val grammar: Grammar, val outcome: String, val tree: ParseNode?)

    private fun seq(vararg e: ParsingExpression) = SequenceExpression(*e)
    private fun alt(vararg e: ParsingExpression) = FirstOfExpression(*e)
    private fun s(x: String) = StringExpression(x)
    private fun nextNot(vararg e: ParsingExpression) = NextNotExpression(SequenceExpression(*e))
    private fun with(value: Int, e: ParsingExpression) = ContextExpression(key, value, true, e)

    private fun parse(retained: Set<String>, input: String, mode: Mode, define: Grammar.() -> ParsingExpression): Run {
        val g = Grammar(retained)
        val body = g.define()
        val root = RuleDefinition("ROOT").apply {
            expression = when (mode) {
                Mode.PLAIN -> body
                Mode.INACTIVE -> ContextExpression(scopeKey, null, false, body)
                Mode.ACTIVE -> ContextExpression(scopeKey, true, true, body)
            }
        }
        val compiled = MutableGrammarCompiler.compile(root)
        val machine = Machine.createMachine(charArrayOf(), emptyArray(), compiled) { }
        assertThat(machine is RetainingMachine || machine is RetainingContextAwareMachine).isEqualTo(compiled.memoRetention != null)
        assertThat(machine is ContextAwareMachine).isEqualTo(compiled.usesParserContext)
        return try {
            val result = Machine.parse(input.toCharArray(), compiled)
            if (result.isMatched()) {
                Run(g, "matched " + shape(result.getParseTreeRoot()), result.getParseTreeRoot())
            } else {
                Run(g, "error at " + result.getParseError()!!.getErrorIndex(), null)
            }
        } catch (e: GrammarException) {
            Run(g, "exception " + e.message, null)
        }
    }

    /** Parses without and with retention of [retained]; returns the retaining run after checking equal outcomes. */
    private fun compare(retained: Set<String>, input: String, mode: Mode, define: Grammar.() -> ParsingExpression): Pair<Run, Run> {
        val baseline = parse(emptySet(), input, mode, define)
        val retaining = parse(retained, input, mode, define)
        assertThat(retaining.outcome).describedAs("mode %s", mode).isEqualTo(baseline.outcome)
        return baseline to retaining
    }

    @Test
    fun displacedResultIsReusedInsteadOfExecutedAgain() {
        for (mode in Mode.entries) {
            val capture = Capture()
            val (baseline, retaining) = compare(setOf("A"), "aby", mode) {
                define("A", s("a"))
                define("C", rule("A"), capture, s("b"))
                alt(seq(rule("C"), s("x")), seq(rule("A"), s("b"), s("y")))
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0, 0)
            assertThat(retaining.grammar.runs("A")).containsExactly(0)
            // the parse tree holds the very node built inside C, not an equal copy
            assertThat(retaining.tree!!.children[0]).isSameAs(capture.nodes.last())
        }
    }

    @Test
    fun ordinaryMemoHitNeedsNoRetainedResult() {
        for (mode in Mode.entries) {
            val (baseline, retaining) = compare(setOf("A"), "ay", mode) {
                define("A", s("a"))
                alt(seq(rule("A"), s("x")), seq(rule("A"), s("y")))
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0)
            assertThat(retaining.grammar.runs("A")).containsExactly(0)
        }
    }

    @Test
    fun ruleWithoutRetentionIsExecutedAgain() {
        for (mode in Mode.entries) {
            val (_, retaining) = compare(setOf("A"), "nby", mode) {
                define("A", s("a"))
                define("N", s("n"))
                define("C", rule("N"), s("b"))
                alt(seq(rule("C"), s("x")), seq(rule("N"), s("b"), s("y")))
            }
            assertThat(retaining.grammar.runs("N")).containsExactly(0, 0)
        }
    }

    @Test
    fun twoRetainedRulesAtOnePositionDoNotDisplaceEachOther() {
        for (mode in Mode.entries) {
            val (baseline, retaining) = compare(setOf("A", "B"), "abcz", mode) {
                define("A", s("a"))
                define("B", rule("A"), s("b"))
                define("C", rule("B"), s("c"))
                // B displaces A, C displaces B; both are then requested again at 0
                alt(
                    seq(rule("C"), s("x")),
                    seq(rule("B"), s("c"), s("y")),
                    seq(rule("A"), s("b"), s("c"), s("z"))
                )
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0, 0, 0)
            assertThat(baseline.grammar.runs("B")).containsExactly(0, 0)
            assertThat(retaining.grammar.runs("A")).containsExactly(0)
            assertThat(retaining.grammar.runs("B")).containsExactly(0)
        }
    }

    @Test
    fun retainedResultsAtDifferentPositions() {
        for (mode in Mode.entries) {
            val (baseline, retaining) = compare(setOf("A", "B"), "abbay", mode) {
                define("A", s("a"))
                define("B", s("b"))
                define("C", rule("A"), s("b"))
                define("D", rule("B"), s("a"))
                alt(
                    seq(rule("C"), rule("D"), s("x")),
                    seq(rule("A"), s("b"), rule("B"), s("a"), s("y"))
                )
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0, 0)
            assertThat(baseline.grammar.runs("B")).containsExactly(2, 2)
            assertThat(retaining.grammar.runs("A")).containsExactly(0)
            assertThat(retaining.grammar.runs("B")).containsExactly(2)
        }
    }

    @Test
    fun zeroLengthResultIsRetained() {
        for (mode in Mode.entries) {
            val (baseline, retaining) = compare(setOf("A"), "by", mode) {
                define("A", OptionalExpression(s("a")))
                define("C", rule("A"), s("b"))
                alt(seq(rule("C"), s("x")), seq(rule("A"), s("b"), s("y")))
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0, 0)
            assertThat(retaining.grammar.runs("A")).containsExactly(0)
        }
    }

    @Test
    fun equalParserContextReusesAndOtherContextExecutes() {
        val (_, equal) = compare(setOf("A"), "aby", Mode.PLAIN) {
            define("A", s("a"))
            define("C", rule("A"), s("b"))
            // two separately entered scopes build equal contexts
            alt(with(1, seq(rule("C"), s("x"))), with(1, seq(rule("A"), s("b"), s("y"))))
        }
        assertThat(equal.grammar.runs("A")).containsExactly(0)

        val (_, different) = compare(setOf("A"), "aby", Mode.PLAIN) {
            define("A", s("a"))
            define("C", rule("A"), s("b"))
            alt(with(1, seq(rule("C"), s("x"))), with(2, seq(rule("A"), s("b"), s("y"))))
        }
        assertThat(different.grammar.runs("A")).containsExactly(0, 0)

        val (_, outside) = compare(setOf("A"), "aby", Mode.PLAIN) {
            define("A", s("a"))
            define("C", rule("A"), s("b"))
            alt(with(1, seq(rule("C"), s("x"))), seq(rule("A"), s("b"), s("y")))
        }
        assertThat(outside.grammar.runs("A")).containsExactly(0, 0)
    }

    @Test
    fun firstContextActivationDropsRetainedResults() {
        val (baseline, retaining) = compare(setOf("A"), "aby", Mode.INACTIVE) {
            define("A", s("a"))
            define("C", rule("A"), s("b"))
            // A is displaced before any context exists; the second alternative activates context first
            alt(seq(rule("C"), s("x")), seq(with(1, OptionalExpression(s("q"))), rule("A"), s("b"), s("y")))
        }
        assertThat(baseline.grammar.runs("A")).containsExactly(0, 0)
        assertThat(retaining.grammar.runs("A")).containsExactly(0, 0)
    }

    @Test
    fun onlyTheLatestDisplacedResultOfARuleIsKeptPerPosition() {
        val (baseline, retaining) = compare(setOf("A"), "abz", Mode.PLAIN) {
            define("A", s("a"))
            define("C", rule("A"), s("b"))
            alt(
                with(1, seq(rule("C"), s("x"))), // retains A under context 1
                with(2, seq(rule("C"), s("x"))), // executes A under context 2, which replaces the retained A
                with(2, seq(rule("A"), s("b"), s("y"))), // reuses A under context 2
                with(1, seq(rule("A"), s("b"), s("z"))) // A under context 1 is gone and runs again
            )
        }
        assertThat(baseline.grammar.runs("A")).containsExactly(0, 0, 0, 0)
        assertThat(retaining.grammar.runs("A")).containsExactly(0, 0, 0)
    }

    @Test
    fun displacedResultKeepsTheContextItWasCreatedIn() {
        // D replaces A's memo while the context is 2, but A was created under context 1
        val (baseline, notReusable) = compare(setOf("A"), "aby", Mode.PLAIN) {
            define("A", s("a"))
            define("D", s("a"), s("b"))
            alt(
                with(1, seq(rule("A"), s("q"))),
                with(2, seq(rule("D"), s("q"))),
                with(2, seq(rule("A"), s("q"))),
                with(2, seq(rule("A"), s("b"), s("y")))
            )
        }
        assertThat(baseline.grammar.runs("A")).containsExactly(0, 0)
        assertThat(notReusable.grammar.runs("A")).containsExactly(0, 0)

        val (reusableBaseline, reusable) = compare(setOf("A"), "aby", Mode.PLAIN) {
            define("A", s("a"))
            define("D", s("a"), s("b"))
            alt(
                with(1, seq(rule("A"), s("q"))),
                with(2, seq(rule("D"), s("q"))),
                with(1, seq(rule("A"), s("b"), s("y")))
            )
        }
        assertThat(reusableBaseline.grammar.runs("A")).containsExactly(0, 0)
        assertThat(reusable.grammar.runs("A")).containsExactly(0)
    }

    @Test
    fun displacedResultKeepsItsOwnErrorReportingState() {
        for (mode in Mode.entries) {
            // A is created while errors are ignored and replaced by D, which is created while errors are reported
            val (_, createdIgnoring) = compare(setOf("A"), "aby", mode) {
                define("A", s("a"))
                define("D", s("a"), s("b"))
                alt(
                    seq(nextNot(rule("A"), s("q")), rule("D"), s("q")),
                    seq(rule("A"), s("b"), s("y"))
                )
            }
            assertThat(createdIgnoring.grammar.runs("A")).containsExactly(0, 0)

            // A is created while errors are reported and replaced by D, which is created while errors are ignored
            val (_, createdReporting) = compare(setOf("A"), "aby", mode) {
                define("A", s("a"))
                define("D", s("a"), s("b"))
                alt(
                    seq(rule("A"), s("q")),
                    seq(nextNot(rule("D"), s("q")), rule("A"), s("b"), s("y"))
                )
            }
            assertThat(createdReporting.grammar.runs("A")).containsExactly(0)
        }
    }

    @Test
    fun errorReportingStateIsTrackedPerPositionAcrossBitsetWords() {
        for (mode in Mode.entries) {
            // positions 1 and 65 share a bit index modulo 64; only the result at 1 was created while errors were ignored
            val input = "x" + "ab" + "x".repeat(62) + "ab" + "y"
            val (baseline, retaining) = compare(setOf("A"), input, mode) {
                define("A", s("a"))
                define("C", rule("A"), s("b"))
                val gap = ZeroOrMoreExpression(s("x"))
                alt(
                    seq(s("x"), nextNot(rule("C"), s("q")), s("a"), s("b"), gap, rule("C"), s("!")),
                    seq(s("x"), rule("A"), s("b"), gap, rule("A"), s("b"), s("y"))
                )
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(1, 65, 1, 65)
            assertThat(retaining.grammar.runs("A")).containsExactly(1, 65, 1)
        }
    }

    @Test
    fun resultCreatedWhileIgnoringErrorsIsOnlyReusedWhileIgnoringErrors() {
        for (mode in Mode.entries) {
            // nextNot ignores errors; C matches inside it and displaces A, then "x" fails so nextNot succeeds
            val (_, outside) = compare(setOf("A"), "aby", mode) {
                define("A", s("a"))
                define("C", rule("A"), s("b"))
                seq(nextNot(rule("C"), s("x")), rule("A"), s("b"), s("y"))
            }
            assertThat(outside.grammar.runs("A")).containsExactly(0, 0)

            val (baseline, inside) = compare(setOf("A"), "aby", mode) {
                define("A", s("a"))
                define("C", rule("A"), s("b"))
                seq(nextNot(rule("C"), s("x")), nextNot(rule("A"), s("q")), s("a"), s("b"), s("y"))
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0, 0)
            assertThat(inside.grammar.runs("A")).containsExactly(0)

            val (_, reported) = compare(setOf("A"), "aby", mode) {
                define("A", s("a"))
                define("C", rule("A"), s("b"))
                alt(seq(rule("C"), s("x")), seq(nextNot(rule("A"), s("q")), s("a"), s("b"), s("y")))
            }
            assertThat(reported.grammar.runs("A")).containsExactly(0)
        }
    }

    @Test
    fun errorPositionReachedOnlyWhenTheRuleRunsWithErrorReporting() {
        for (mode in Mode.entries) {
            // D fails at index 3 inside A; that index is reported only if A runs outside nextNot
            val (baseline, retaining) = compare(setOf("A"), "abcz", mode) {
                define("D", s("b"), s("c"), s("d"))
                define("A", s("a"), alt(rule("D"), s("b")))
                define("C", rule("A"))
                seq(nextNot(rule("C"), s("x")), rule("A"), s("y"))
            }
            assertThat(baseline.outcome).isEqualTo("error at 3")
            assertThat(retaining.grammar.runs("A")).containsExactly(0, 0)
        }
    }

    @Test
    fun leftRecursionIsReportedLikeWithoutRetention() {
        for (mode in Mode.entries) {
            val (_, direct) = compare(setOf("A"), "ab", mode) {
                define("A", alt(seq(rule("A"), s("b")), s("a")))
                rule("A")
            }
            assertThat(direct.outcome).isEqualTo("exception Left recursion has been detected, involved rule: A")

            val (_, indirect) = compare(setOf("A", "B"), "ab", mode) {
                define("A", alt(seq(rule("B"), s("b")), s("a")))
                define("B", rule("A"))
                rule("A")
            }
            assertThat(indirect.outcome).isEqualTo("exception Left recursion has been detected, involved rule: A")

            // a recursive rule reused from the retained store at 0 still nests through ordinary memo hits
            val (baseline, nested) = compare(setOf("A"), "((a))", mode) {
                define("A", alt(seq(s("("), rule("A"), s(")")), s("a")))
                define("D", rule("A"))
                alt(seq(rule("D"), s("!")), rule("A"))
            }
            assertThat(baseline.grammar.runs("A")).containsExactly(0, 1, 2, 0)
            assertThat(nested.grammar.runs("A")).containsExactly(0, 1, 2)
        }
    }

    @Test
    fun rootRuleIsRetainedOnlyAsACalledTarget() {
        val root = RuleDefinition("ROOT").apply { enableMemoRetention() }
        root.expression = s("a")
        val alone = MutableGrammarCompiler.compile(root)
        assertThat(alone.memoRetention).isNull()
        assertThat(Machine.createMachine(charArrayOf(), emptyArray(), alone) { }).isExactlyInstanceOf(Machine::class.java)

        val nested = RuleDefinition("ROOT").apply { enableMemoRetention() }
        nested.expression = alt(seq(s("("), nested, s(")")), s("a"))
        val called = MutableGrammarCompiler.compile(nested)
        assertThat(called.memoRetention!!.slots).isEqualTo(1)
        assertThat(called.memoRetention!!.slotByTarget).containsExactly(0)
        assertThat(Machine.parse("((a))".toCharArray(), called).isMatched()).isTrue()
    }

    @Test
    fun anonymousAndSharedTargetsResolveToDenseSlots() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Key.A).`is`("a")
        b.rule(Key.A).enableMemoRetention()
        b.rule(Key.B).`is`("b")
        b.rule(Key.B).enableMemoRetention()
        b.rule(Key.N).`is`("n")
        // A has three call sites; token() adds an anonymous call target
        b.rule(Key.ROOT).`is`(b.firstOf(b.sequence(Key.A, Key.N), b.sequence(Key.A, Key.B), b.token(GenericTokenType.LITERAL, Key.A)))
        b.setRootRule(Key.ROOT)
        val compiled = MutableGrammarCompiler.compile(b.build().rootRule as CompilableGrammarRule)
        val retention = compiled.memoRetention!!
        val targets = compiled.instructions.filterIsInstance<Instruction.CallInstruction>()
            .associate { it.targetId to it.matcher }
        assertThat(retention.slots).isEqualTo(2)
        for ((id, matcher) in targets) {
            val expected = when (matcher.toString()) {
                "A" -> 0
                "B" -> 1
                else -> -1
            }
            assertThat(retention.slotByTarget[id]).describedAs("target %s", matcher).isEqualTo(expected)
        }
        assertThat(targets.values).anyMatch { it is TokenExpression }
        assertThat(Machine.parse("ab".toCharArray(), compiled).isMatched()).isTrue()
    }

    @Test
    fun grammarInstancesAreIndependent() {
        fun build(retain: Boolean): CompiledGrammar {
            val b = LexerlessGrammarBuilder.create()
            b.rule(Key.A).`is`("a")
            if (retain) b.rule(Key.A).enableMemoRetention()
            b.rule(Key.ROOT).`is`(Key.A)
            b.setRootRule(Key.ROOT)
            return MutableGrammarCompiler.compile(b.build().rootRule as CompilableGrammarRule)
        }
        val retaining = build(true)
        val ordinary = build(false)
        assertThat(retaining.memoRetention).isNotNull()
        assertThat(ordinary.memoRetention).isNull()
        assertThat(Machine.createMachine(charArrayOf(), emptyArray(), retaining) { }).isExactlyInstanceOf(RetainingMachine::class.java)
        assertThat(Machine.createMachine(charArrayOf(), emptyArray(), ordinary) { }).isExactlyInstanceOf(Machine::class.java)
    }

    @Test
    fun retentionRequestedAfterCompilationDoesNotChangeTheCompiledGrammar() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Key.A).`is`("a")
        b.rule(Key.ROOT).`is`(Key.A)
        b.setRootRule(Key.ROOT)
        val compiled = MutableGrammarCompiler.compile(b.build().rootRule as CompilableGrammarRule)
        b.rule(Key.A).enableMemoRetention()
        assertThat(compiled.memoRetention).isNull()
        assertThat(Machine.createMachine(charArrayOf(), emptyArray(), compiled) { }).isExactlyInstanceOf(Machine::class.java)
    }

    @Test
    fun ruleImplementationWithoutRetentionSupportFailsClearly() {
        val unsupported = mock<CompilableGrammarRule>()
        whenever(unsupported.ruleKey).thenReturn(Key.A)
        val builder = GrammarBuilder.RuleBuilder(LexerlessGrammarBuilder.create(), unsupported)
        assertThrows<GrammarException> { builder.enableMemoRetention() }
            .also { assertThat(it.message).isEqualTo("The rule 'A' does not support memo retention.") }
    }

    @Test
    fun memoizationIsEnabledWithRetention() {
        val b = LexerfulGrammarBuilder.create()
        b.rule(Key.A).`is`("a")
        b.rule(Key.A).enableMemoRetention()
        b.rule(Key.N).`is`("n")
        b.rule(Key.ROOT).`is`(Key.A, Key.N)
        b.setRootRule(Key.ROOT)
        val grammar = b.build()
        val a = grammar.rule(Key.A) as RuleDefinition
        val n = grammar.rule(Key.N) as RuleDefinition
        assertThat(a.shouldMemoize()).isTrue()
        assertThat(a.shouldRetainMemo()).isTrue()
        assertThat(n.shouldMemoize()).isFalse()
        assertThat(n.shouldRetainMemo()).isFalse()
    }

    @Test
    fun lexerfulGrammarsReuseRetainedResults() {
        for (contextual in listOf(false, true)) {
            fun parse(retain: Boolean, values: String): Pair<String, List<Int>> {
                val entries = Entries()
                val b = LexerfulGrammarBuilder.create()
                b.rule(Key.A).`is`(entries, "a")
                if (retain) b.rule(Key.A).enableMemoRetention()
                b.rule(Key.C).`is`(Key.A, "b")
                val body = b.firstOf(b.sequence(Key.C, "x"), b.sequence(Key.A, "b", "y"))
                b.rule(Key.ROOT).`is`(if (contextual) b.withContext(key, 1, body) else body)
                b.setRootRule(Key.ROOT)
                val grammar = b.buildWithMemoizationOfMatchesForAllRules()
                val compiled = MutableGrammarCompiler.compile(grammar.rootRule as CompilableGrammarRule)
                assertThat(compiled.usesParserContext).isEqualTo(contextual)
                val tokens = values.mapIndexed { i, c -> token(i, c.toString()) } + token(values.length, "EOF")
                val outcome = try {
                    "matched " + shape(Machine.parse(tokens, compiled))
                } catch (e: RecognitionException) {
                    "error " + e.message
                }
                return outcome to entries.positions
            }
            for (input in listOf("aby", "abq")) {
                val (baseline, baselineRuns) = parse(false, input)
                val (retaining, retainingRuns) = parse(true, input)
                assertThat(retaining).isEqualTo(baseline)
                assertThat(baselineRuns).containsExactly(0, 0)
                assertThat(retainingRuns).containsExactly(0)
            }
        }
    }

    @Test
    fun profiledRetentionCountsRetainedReuseAsHit() {
        for (mode in Mode.entries) {
            val g = Grammar(setOf("A"))
            g.define("A", s("a"))
            g.define("C", g.rule("A"), s("b"))
            val body = alt(seq(g.rule("C"), s("x")), seq(g.rule("A"), s("b"), s("y")))
            val root = RuleDefinition("ROOT").apply {
                expression = when (mode) {
                    Mode.PLAIN -> body
                    Mode.INACTIVE -> ContextExpression(scopeKey, null, false, body)
                    Mode.ACTIVE -> ContextExpression(scopeKey, true, true, body)
                }
            }
            val compiled = MutableGrammarCompiler.compile(root)
            val machine = Machine.createMachine(charArrayOf(), emptyArray(), compiled, { }, ParsingProfiler().countersFor(compiled))
            assertThat(machine).isExactlyInstanceOf(
                if (mode == Mode.PLAIN) ProfilingRetainingMachine::class.java else ProfilingRetainingContextAwareMachine::class.java
            )
            val profiler = ParsingProfiler()
            val result = ParseRunner(root).parse("aby".toCharArray(), profiler)
            assertThat(result.isMatched()).isTrue()
            assertThat(g.runs("A")).containsExactly(0)
            val a = profiler.snapshot().programs.single().targets.single { it.name == "A" }
            assertThat(listOf(a.memoHits, a.memoEmptyMisses, a.memoMatcherMisses, a.memoContextMisses))
                .containsExactly(1L, 1L, 0L, 0L)
            assertThat(a.executedInvocations).isEqualTo(1L)
            assertThat(a.failures).isEqualTo(0L)
            assertThat(a.memoOverwritesOther).isEqualTo(0L)
            assertThat(profiler.snapshot().programs.single().targets.single { it.name == "C" }.memoOverwritesOther)
                .isEqualTo(1L)
        }
    }

    @Test
    fun machineSelectionCoversEveryRetentionContextAndProfilingCombination() {
        for (retain in listOf(false, true)) {
            for (contextual in listOf(false, true)) {
                val b = LexerlessGrammarBuilder.create()
                b.rule(Key.A).`is`("a")
                if (retain) b.rule(Key.A).enableMemoRetention()
                b.rule(Key.ROOT).`is`(if (contextual) b.withContext(key, 1, Key.A) else Key.A)
                b.setRootRule(Key.ROOT)
                val compiled = MutableGrammarCompiler.compile(b.build().rootRule as CompilableGrammarRule)
                assertThat(compiled.memoRetention != null).isEqualTo(retain)
                val plain = Machine.createMachine(charArrayOf(), emptyArray(), compiled) { }
                val profiled = Machine.createMachine(
                    charArrayOf(), emptyArray(), compiled, { }, ParsingProfiler().countersFor(compiled)
                )
                val expected = when {
                    !retain && !contextual -> Machine::class.java to ProfilingMachine::class.java
                    !retain -> ContextAwareMachine::class.java to ProfilingContextAwareMachine::class.java
                    !contextual -> RetainingMachine::class.java to ProfilingRetainingMachine::class.java
                    else -> RetainingContextAwareMachine::class.java to ProfilingRetainingContextAwareMachine::class.java
                }
                assertThat(plain).describedAs("retain=%s context=%s", retain, contextual).isExactlyInstanceOf(expected.first)
                assertThat(profiled).describedAs("retain=%s context=%s", retain, contextual).isExactlyInstanceOf(expected.second)
            }
        }
    }

    private enum class Key : GrammarRuleKey { ROOT, A, B, C, N }

    private class Entries : NativeExpression() {
        val positions = mutableListOf<Int>()
        override fun execute(machine: Machine) {
            positions.add(machine.index)
            machine.jump(1)
        }
    }

    /** Records the node most recently added to the current frame. */
    private class Capture : NativeExpression() {
        val nodes = mutableListOf<ParseNode>()
        override fun execute(machine: Machine) {
            nodes.add(machine.peek().subNodes.last())
            machine.jump(1)
        }
    }

    private fun shape(n: ParseNode): String =
        "${n.matcher}[${n.startIndex},${n.endIndex}](${n.children.joinToString(",") { shape(it) }})"

    private fun token(column: Int, value: String): Token = Token.builder()
        .setType(if (value == "EOF") GenericTokenType.EOF else GenericTokenType.IDENTIFIER)
        .setValueAndOriginalValue(value)
        .setLine(1)
        .setColumn(column)
        .build()
}
