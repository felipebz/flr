//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.ContextKey
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ParserContextSpecializationTest {
    @Test
    fun grammar_without_context_selects_ordinary_vm_and_instructions() {
        val grammar = compile(contextAware = false)

        assertThat(grammar.usesParserContext).isFalse()
        assertThat(Machine.createMachine(charArrayOf(), emptyArray(), grammar) { }).isExactlyInstanceOf(Machine::class.java)
        assertThat(grammar.instructions).anyMatch { it is Instruction.BackCommitInstruction }
        assertThat(grammar.instructions).noneMatch { it is ContextBackCommitInstruction }
        assertThat(grammar.instructions).noneMatch { it is ContextFailTwiceInstruction }
    }

    @Test
    fun grammar_with_context_selects_context_vm_and_instructions() {
        val grammar = compile(contextAware = true)

        assertThat(grammar.usesParserContext).isTrue()
        assertThat(Machine.createMachine(charArrayOf(), emptyArray(), grammar) { }).isInstanceOf(ContextAwareMachine::class.java)
        assertThat(grammar.instructions).anyMatch { it is ContextBackCommitInstruction }
        assertThat(grammar.instructions).anyMatch { it is ContextFailTwiceInstruction }
    }

    private fun compile(contextAware: Boolean): CompiledGrammar {
        val b = LexerlessGrammarBuilder.create()
        val atom = if (contextAware) {
            val key = ContextKey<Boolean>()
            b.withContext(key, true, b.requireContext(key), "a")
        } else {
            "a"
        }
        b.rule(Rules.ATOM).`is`(atom)
        b.rule(Rules.ROOT).`is`(b.next(Rules.ATOM), b.nextNot("b"), Rules.ATOM, b.endOfInput())
        b.setRootRule(Rules.ROOT)
        return MutableGrammarCompiler.compile(b.build().rootRule as CompilableGrammarRule)
    }

    private enum class Rules : GrammarRuleKey {
        ROOT,
        ATOM
    }
}
