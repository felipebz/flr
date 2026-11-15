//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Trivia.TriviaKind
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.internal.vm.Machine.Companion.execute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit

// TODO this test should also check state of machine after execution
@Timeout(value = 5, unit = TimeUnit.SECONDS)
class MachineIntegrationTest {

    @Test
    fun pattern() {
        val instructions = InstructionProgram.link(PatternExpression("foo|bar").compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isTrue()
        assertThat(execute("baz", instructions)).isFalse()
    }

    @Test
    fun string() {
        val instructions = InstructionProgram.link(StringExpression("foo").compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }

    @Test
    fun sequence() {
        val instructions = InstructionProgram.link(SequenceExpression(
            StringExpression("foo"), StringExpression("bar")
        ).compile(CompilationHandler()))
        assertThat(execute("foobar", instructions)).isTrue()
        assertThat(execute("baz", instructions)).isFalse()
    }

    @Test
    fun firstOf() {
        val instructions = InstructionProgram.link(FirstOfExpression(
            StringExpression("foo"),
            StringExpression("bar"),
            StringExpression("baz")
        ).compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isTrue()
        assertThat(execute("baz", instructions)).isTrue()
        assertThat(execute("qux", instructions)).isFalse()
    }

    @Test
    fun optional() {
        val instructions = InstructionProgram.link(OptionalExpression(StringExpression("a")).compile(CompilationHandler()))
        assertThat(execute("", instructions)).isTrue()
        assertThat(execute("a", instructions)).isTrue()
    }

    @Test
    operator fun next() {
        val instructions = InstructionProgram.link(NextExpression(StringExpression("foo")).compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }

    @Test
    fun nextNot() {
        val instructions = InstructionProgram.link(NextNotExpression(StringExpression("foo")).compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isFalse()
        assertThat(execute("bar", instructions)).isTrue()
    }

    @Test
    fun zeroOrMore() {
        val instructions = InstructionProgram.link(ZeroOrMoreExpression(StringExpression("a")).compile(CompilationHandler()))
        assertThat(execute("", instructions)).isTrue()
        assertThat(execute("a", instructions)).isTrue()
        assertThat(execute("aa", instructions)).isTrue()
    }

    @Test
    fun zeroOrMore_should_not_cause_infinite_loop() {
        val instructions = InstructionProgram.link(ZeroOrMoreExpression(
            FirstOfExpression(
                StringExpression("foo"),
                StringExpression("")
            )
        ).compile(CompilationHandler()))
        assertThrows<GrammarException>("The inner part of ZeroOrMore and OneOrMore must not allow empty matches") {
            execute("foo", instructions)
        }
    }

    @Test
    fun oneOrMore() {
        val instructions = InstructionProgram.link(OneOrMoreExpression(StringExpression("a")).compile(CompilationHandler()))
        assertThat(execute("", instructions)).isFalse()
        assertThat(execute("a", instructions)).isTrue()
        assertThat(execute("aa", instructions)).isTrue()
    }

    @Test
    fun oneOrMore_should_not_cause_infinite_loop() {
        val instructions = InstructionProgram.link(OneOrMoreExpression(
            FirstOfExpression(
                StringExpression("foo"),
                StringExpression("")
            )
        ).compile(CompilationHandler()))
        assertThrows<GrammarException>("The inner part of ZeroOrMore and OneOrMore must not allow empty matches") {
            execute("foo", instructions)
        }
    }

    @Test
    fun token() {
        val instructions =
            InstructionProgram.link(TokenExpression(GenericTokenType.IDENTIFIER, StringExpression("foo")).compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }

    @Test
    fun trivia() {
        val instructions = InstructionProgram.link(TriviaExpression(TriviaKind.COMMENT, StringExpression("foo")).compile(CompilationHandler()))
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }
}
