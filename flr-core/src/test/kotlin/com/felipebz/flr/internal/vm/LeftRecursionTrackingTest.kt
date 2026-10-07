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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Trivia.TriviaKind
import com.felipebz.flr.grammar.ContextKey
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.impl.matcher.RuleDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LeftRecursionTrackingTest {
    @Test
    fun initialRootRemainsUntrackedForDirectAndIndirectRecursion() {
        for (contextual in listOf(false, true)) {
            val directEntries = Entries()
            val a = RuleDefinition("A")
            a.expression = scoped(SequenceExpression(directEntries, a), contextual)
            val directGrammar = MutableGrammarCompiler.compile(a)
            assertThat(directGrammar.callTargetCount).isEqualTo(1)
            val direct = assertThrows<GrammarException> { Machine.parse(charArrayOf(), directGrammar) }
            assertThat(direct.message).isEqualTo(diagnostic("A"))
            assertThat(directEntries.positions).containsExactly(0, 0)

            val aEntries = Entries()
            val bEntries = Entries()
            val first = RuleDefinition("A")
            val second = RuleDefinition("B")
            first.expression = scoped(SequenceExpression(aEntries, second), contextual)
            second.expression = SequenceExpression(bEntries, first)
            val indirectGrammar = MutableGrammarCompiler.compile(first)
            assertThat(indirectGrammar.callTargetCount).isEqualTo(2)
            val indirect = assertThrows<GrammarException> { Machine.parse(charArrayOf(), indirectGrammar) }
            assertThat(indirect.message).isEqualTo(diagnostic("B"))
            assertThat(aEntries.positions).containsExactly(0, 0)
            assertThat(bEntries.positions).containsExactly(0)
        }
    }

    @Test
    fun nonrecursiveRootUsesOnlyItsCalledChildTarget() {
        for (contextual in listOf(false, true)) {
            val child = RuleDefinition("CHILD").apply { expression = StringExpression("x") }
            val compiled = MutableGrammarCompiler.compile(root(SequenceExpression(child, EndOfInputExpression), contextual))
            assertThat(compiled.callTargetCount).isEqualTo(1)
            val result = Machine.parse("x".toCharArray(), compiled)
            assertThat(result.isMatched()).isTrue()
            assertThat(result.getParseTreeRoot().children.first().matcher).isSameAs(child)
            assertThat(Machine.parse("y".toCharArray(), compiled).isMatched()).isFalse()
        }
    }

    @Test
    fun zeroCallProgramsAndRootsHaveNoTrackingSlots() {
        val program = InstructionProgram.link(arrayOf(StringExpression("x"), EndOfInputExpression, Instruction.end()))
        assertThat(program.callTargetCount).isZero()
        assertThat(ObservedMachine("x", program).stateSlotCount).isZero()
        assertThat(Machine.execute("x", program)).isTrue()
        assertThat(Machine.execute("xy", program)).isFalse()
        for (contextual in listOf(false, true)) {
            val compiled = MutableGrammarCompiler.compile(root(SequenceExpression(StringExpression("x"), EndOfInputExpression), contextual))
            assertThat(compiled.callTargetCount).isZero()
            assertThat(Machine.parse("x".toCharArray(), compiled).isMatched()).isTrue()
            assertThat(Machine.parse("y".toCharArray(), compiled).isMatched()).isFalse()
        }
    }

    @Test
    fun differentCallSitesRejectTheSecondEntryIntoOneActiveTarget() {
        for (contextual in listOf(false, true)) {
            val entries = Entries()
            val a = RuleDefinition("A")
            a.expression = SequenceExpression(entries, a)
            val root = root(a, contextual)
            val error = assertThrows<GrammarException> { parse(root, "") }
            assertThat(error.message).isEqualTo(diagnostic("A"))
            // ROOT->A and A->A are different instructions. Call-site tracking
            // would execute A's body twice before detecting recursion.
            assertThat(entries.positions).containsExactly(0)
        }
    }

    @Test
    fun consumingNestedCallsToTheSameTargetRemainValid() {
        for (contextual in listOf(false, true)) {
            for (memoized in listOf(false, true)) {
                val entries = Entries()
                val a = rule("A", memoized)
                a.expression = SequenceExpression(entries, FirstOfExpression(
                    SequenceExpression(StringExpression("a"), a), StringExpression("x")
                ))
                val result = parse(root(SequenceExpression(a, EndOfInputExpression), contextual), "aax")
                assertThat(result.isMatched()).isTrue()
                assertThat(result.getParseTreeRoot().endIndex).isEqualTo(3)
                assertThat(entries.positions).containsExactly(0, 1, 2)
            }
        }
    }

    @Test
    fun nestedReturnRestoresTheOuterPositionBeforeBacktrackingReentry() {
        for (contextual in listOf(false, true)) {
            for (memoized in listOf(false, true)) {
                val entries = Entries()
                val a = rule("A", memoized)
                a.expression = SequenceExpression(entries, FirstOfExpression(
                    SequenceExpression(StringExpression("a"), a, StringExpression("!")),
                    StringExpression("x"),
                    a
                ))
                val error = assertThrows<GrammarException> { parse(root(a, contextual), "ax") }
                assertThat(error.message).isEqualTo(diagnostic("A"))
                // The index-1 invocation returned before ! failed. Its return
                // must restore index 0, not clear the active outer invocation.
                assertThat(entries.positions).containsExactly(0, 1)
            }
        }
    }

    @Test
    fun failedNestedCallsAndChoiceRetryDoNotLeaveStaleState() {
        for (contextual in listOf(false, true)) {
            for (memoized in listOf(false, true)) {
                val entries = Entries()
                val a = rule("A", memoized)
                a.expression = SequenceExpression(entries, FirstOfExpression(
                    SequenceExpression(StringExpression("a"), a, StringExpression("!")),
                    StringExpression("a")
                ))
                val expression = SequenceExpression(FirstOfExpression(
                    SequenceExpression(a, StringExpression("!")),
                    SequenceExpression(a, StringExpression("a?"))
                ), EndOfInputExpression)
                val compiled = MutableGrammarCompiler.compile(root(expression, contextual))
                repeat(2) {
                    entries.positions.clear()
                    val result = Machine.parse("aa?".toCharArray(), compiled)
                    assertThat(result.isMatched()).isTrue()
                    assertThat(result.getParseTreeRoot().endIndex).isEqualTo(3)
                    if (memoized) {
                        assertThat(entries.positions).containsExactly(0, 1, 2)
                    } else {
                        assertThat(entries.positions).containsExactly(0, 1, 2, 0, 1, 2)
                    }
                }
            }
        }
    }

    @Test
    fun contextChangesDoNotSplitOneTargetsRecursionState() {
        val stage = ContextKey<String>()
        val entries = Entries()
        val a = rule("A", true)
        a.expression = SequenceExpression(entries, FirstOfExpression(
            SequenceExpression(ContextPredicateExpression(stage, "one", false),
                ContextExpression(stage, "two", true, a)),
            SequenceExpression(ContextPredicateExpression(stage, "two", false),
                ContextExpression(stage, "done", true, a)),
            SequenceExpression(ContextPredicateExpression(stage, "done", false), StringExpression("x"))
        ))
        val error = assertThrows<GrammarException> {
            parse(root(ContextExpression(stage, "one", true, a)), "x")
        }
        assertThat(error.message).isEqualTo(diagnostic("A"))
        // A call-site or context-keyed guard could reach the finite done branch.
        assertThat(entries.positions).containsExactly(0)
    }

    @Test
    fun contextSensitiveMemoHitAndMissKeepTargetTrackingIndependent() {
        for (sameContext in listOf(false, true)) {
            val stage = ContextKey<String>()
            val entries = Entries()
            val a = rule("A", true)
            a.expression = SequenceExpression(entries, ContextPredicateExpression(stage, "one", false),
                StringExpression("x"))
            val expression = SequenceExpression(
                ContextExpression(stage, "one", true, NextExpression(a)),
                ContextExpression(stage, if (sameContext) "one" else "two", true, a),
                EndOfInputExpression
            )
            val result = parse(root(expression), "x")
            assertThat(result.isMatched()).isEqualTo(sameContext)
            if (sameContext) assertThat(entries.positions).containsExactly(0)
            else assertThat(entries.positions).containsExactly(0, 0)
        }
    }

    @Test
    fun nestedTokenAndTriviaSubroutinesDoNotCollideAtInputZero() {
        val inner = TokenExpression(GenericTokenType.IDENTIFIER, StringExpression("x"))
        val trivia = TriviaExpression(TriviaKind.COMMENT, inner)
        val outer = TokenExpression(GenericTokenType.IDENTIFIER, trivia)
        val compiled = MutableGrammarCompiler.compile(root(outer))
        val result = Machine.parse("x".toCharArray(), compiled)
        assertThat(result.isMatched()).isTrue()
        val outerNode = result.getParseTreeRoot().children.single()
        assertThat(outerNode.matcher).isSameAs(outer)
        val triviaNode = outerNode.children.single()
        assertThat(triviaNode.matcher).isSameAs(trivia)
        assertThat(triviaNode.children.single().matcher).isSameAs(inner)
    }

    @Test
    fun recursiveTokenAndTriviaWrappersKeepTheirDiagnosticAndRejectionPoint() {
        for (trivia in listOf(false, true)) {
            val entries = Entries()
            val a = RuleDefinition("A")
            val wrapper: ParsingExpression = if (trivia) TriviaExpression(TriviaKind.COMMENT, a)
                else TokenExpression(GenericTokenType.IDENTIFIER, a)
            a.expression = SequenceExpression(entries, wrapper)
            val error = assertThrows<GrammarException> { parse(a, "") }
            assertThat(error.message).isEqualTo(diagnostic(if (trivia) "Trivia COMMENT[A]" else "Token IDENTIFIER[A]"))
            assertThat(entries.positions).containsExactly(0, 0)
        }
    }

    @Test
    fun reusedMatcherAtDistinctAnonymousEntriesDoesNotMergeTargets() {
        val matcher = TokenExpression(GenericTokenType.IDENTIFIER, StringExpression("x"))
        val entries = Entries()
        val program = InstructionProgram.link(arrayOf(
            Instruction.call(2, matcher), Instruction.end(),
            entries, Instruction.call(2, matcher), Instruction.ret(),
            entries, StringExpression("x"), Instruction.ret()
        ))
        assertThat(Machine.execute("x", program)).isTrue()
        assertThat(entries.positions).containsExactly(0, 0)
    }

    @Test
    fun zeroStateAndNestedEncodedPositionsAreRestoredExactly() {
        val matcher = RuleDefinition("A")
        val program = InstructionProgram.link(arrayOf(
            Instruction.call(2, matcher), Instruction.call(1, matcher), Instruction.ret()
        ))
        val targetId = (program.instructions[0] as Instruction.CallInstruction).targetId
        val machine = ObservedMachine("xx", program)
        assertThat(machine.state(targetId)).isZero()
        machine.pushReturn(1, matcher, 2, targetId)
        assertThat(machine.state(targetId)).isEqualTo(1)
        machine.index = 1
        machine.address = 1
        machine.pushReturn(1, matcher, 1, targetId)
        assertThat(machine.state(targetId)).isEqualTo(2)
        assertThat(machine.peek().previousCallState).isEqualTo(1)
        machine.popReturn()
        assertThat(machine.state(targetId)).isEqualTo(1)
        machine.popReturn()
        assertThat(machine.state(targetId)).isZero()
        machine.index = 0
        machine.address = 1
        machine.pushReturn(1, matcher, 1, targetId)
        assertThat(machine.state(targetId)).isEqualTo(1)
    }

    @Test
    fun wrappedNegativeEncodedStateIsSavedAndRestoredUnchanged() {
        val matcher = RuleDefinition("A")
        val program = InstructionProgram.link(arrayOf(Instruction.call(1, matcher), Instruction.ret()))
        val targetId = (program.instructions[0] as Instruction.CallInstruction).targetId
        val machine = ObservedMachine("x", program)
        machine.pushReturn(1, matcher, 1, targetId)
        // Int.MAX_VALUE+1 encodes to MIN_VALUE. A real MAX-sized input is
        // already excluded by memos' inputLength+1 capacity; exercise the
        // representable negative saved state through actual VM restoration.
        machine.peek().previousCallState = Int.MIN_VALUE
        machine.popReturn()
        assertThat(machine.state(targetId)).isEqualTo(Int.MIN_VALUE)
        machine.address = 0
        machine.pushReturn(1, matcher, 1, targetId)
        assertThat(machine.peek().previousCallState).isEqualTo(Int.MIN_VALUE)
        assertThat(machine.state(targetId)).isEqualTo(1)
        machine.popReturn()
        assertThat(machine.state(targetId)).isEqualTo(Int.MIN_VALUE)
    }

    private fun rule(name: String, memoized: Boolean): RuleDefinition = RuleDefinition(name).apply {
        if (memoized) enableMemoization()
    }

    private fun root(expression: ParsingExpression, contextual: Boolean = false): RuleDefinition =
        RuleDefinition("ROOT").apply { this.expression = scoped(expression, contextual) }

    private fun scoped(expression: ParsingExpression, contextual: Boolean): ParsingExpression =
        if (contextual) ContextExpression(ContextKey<Boolean>(), true, true, expression) else expression

    private fun parse(root: CompilableGrammarRule, input: String) =
        Machine.parse(input.toCharArray(), MutableGrammarCompiler.compile(root))

    private fun diagnostic(matcher: String) = "Left recursion has been detected, involved rule: $matcher"

    private class Entries : NativeExpression() {
        val positions = mutableListOf<Int>()
        override fun execute(machine: Machine) {
            positions.add(machine.index)
            machine.jump(1)
        }
    }

    private class ObservedMachine(input: String, program: InstructionProgram) : Machine(input, program) {
        val stateSlotCount: Int
            get() = calls.size
        fun state(targetId: Int): Int = calls[targetId]
    }
}
