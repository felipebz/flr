/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.internal.vm

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Trivia.TriviaKind
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.vm.Machine.Companion.execute

// TODO this test should also check state of machine after execution
class MachineIntegrationTest {
    @JvmField
    @Rule
    var timeout = Timeout(5000)

    @Test
    fun pattern() {
        val instructions = PatternExpression("foo|bar").compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isTrue()
        assertThat(execute("baz", instructions)).isFalse()
    }

    @Test
    fun string() {
        val instructions = StringExpression("foo").compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }

    @Test
    fun sequence() {
        val instructions = SequenceExpression(
            StringExpression("foo"), StringExpression("bar")
        ).compile(CompilationHandler())
        assertThat(execute("foobar", instructions)).isTrue()
        assertThat(execute("baz", instructions)).isFalse()
    }

    @Test
    fun firstOf() {
        val instructions = FirstOfExpression(
            StringExpression("foo"),
            StringExpression("bar"),
            StringExpression("baz")
        ).compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isTrue()
        assertThat(execute("baz", instructions)).isTrue()
        assertThat(execute("qux", instructions)).isFalse()
    }

    @Test
    fun optional() {
        val instructions = OptionalExpression(StringExpression("a")).compile(CompilationHandler())
        assertThat(execute("", instructions)).isTrue()
        assertThat(execute("a", instructions)).isTrue()
    }

    @Test
    operator fun next() {
        val instructions = NextExpression(StringExpression("foo")).compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }

    @Test
    fun nextNot() {
        val instructions = NextNotExpression(StringExpression("foo")).compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isFalse()
        assertThat(execute("bar", instructions)).isTrue()
    }

    @Test
    fun zeroOrMore() {
        val instructions = ZeroOrMoreExpression(StringExpression("a")).compile(CompilationHandler())
        assertThat(execute("", instructions)).isTrue()
        assertThat(execute("a", instructions)).isTrue()
        assertThat(execute("aa", instructions)).isTrue()
    }

    @Test
    fun zeroOrMore_should_not_cause_infinite_loop() {
        val instructions = ZeroOrMoreExpression(
            FirstOfExpression(
                StringExpression("foo"),
                StringExpression("")
            )
        ).compile(CompilationHandler())
        assertThrows("The inner part of ZeroOrMore and OneOrMore must not allow empty matches", GrammarException::class.java) {
            execute("foo", instructions)
        }
    }

    @Test
    fun oneOrMore() {
        val instructions = OneOrMoreExpression(StringExpression("a")).compile(CompilationHandler())
        assertThat(execute("", instructions)).isFalse()
        assertThat(execute("a", instructions)).isTrue()
        assertThat(execute("aa", instructions)).isTrue()
    }

    @Test
    fun oneOrMore_should_not_cause_infinite_loop() {
        val instructions = OneOrMoreExpression(
            FirstOfExpression(
                StringExpression("foo"),
                StringExpression("")
            )
        ).compile(CompilationHandler())
        assertThrows("The inner part of ZeroOrMore and OneOrMore must not allow empty matches", GrammarException::class.java) {
            execute("foo", instructions)
        }
    }

    @Test
    fun token() {
        val instructions =
            TokenExpression(GenericTokenType.IDENTIFIER, StringExpression("foo")).compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }

    @Test
    fun trivia() {
        val instructions = TriviaExpression(TriviaKind.COMMENT, StringExpression("foo")).compile(CompilationHandler())
        assertThat(execute("foo", instructions)).isTrue()
        assertThat(execute("bar", instructions)).isFalse()
    }
}