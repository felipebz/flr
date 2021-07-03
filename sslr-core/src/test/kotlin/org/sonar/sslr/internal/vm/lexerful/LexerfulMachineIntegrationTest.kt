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
package org.sonar.sslr.internal.vm.lexerful

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import com.sonar.sslr.api.TokenType
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.sonar.sslr.internal.vm.CompilationHandler
import org.sonar.sslr.internal.vm.Machine.Companion.execute
import org.sonar.sslr.internal.vm.SequenceExpression

class LexerfulMachineIntegrationTest {
    @Test
    fun tokenType() {
        val instructions = TokenTypeExpression(GenericTokenType.IDENTIFIER).compile(CompilationHandler())
        assertThat(execute(instructions, token(GenericTokenType.IDENTIFIER))).isTrue()
        assertThat(execute(instructions, token(GenericTokenType.LITERAL))).isFalse()
    }

    @Test
    fun tokenTypes() {
        val instructions =
            TokenTypesExpression(GenericTokenType.IDENTIFIER, GenericTokenType.LITERAL).compile(CompilationHandler())
        var tokens = arrayOf(token(GenericTokenType.IDENTIFIER))
        assertThat(execute(instructions, *tokens)).isTrue()
        tokens = arrayOf(token(GenericTokenType.LITERAL))
        assertThat(execute(instructions, *tokens)).isTrue()
        tokens = arrayOf(token(GenericTokenType.UNKNOWN_CHAR))
        assertThat(execute(instructions, *tokens)).isFalse()
    }

    @Test
    fun tokenValue() {
        val instructions = TokenValueExpression("foo").compile(CompilationHandler())
        assertThat(execute(instructions, token("foo"))).isTrue()
        assertThat(execute(instructions, token("bar"))).isFalse()
    }

    @Test
    fun anyToken() {
        val instructions = AnyTokenExpression.INSTANCE.compile(CompilationHandler())
        assertThat(execute(instructions, token("foo"))).isTrue()
    }

    @Test
    fun tokensBridge() {
        val instructions =
            TokensBridgeExpression(GenericTokenType.IDENTIFIER, GenericTokenType.LITERAL).compile(CompilationHandler())
        var tokens = arrayOf(token(GenericTokenType.IDENTIFIER), token(GenericTokenType.LITERAL))
        assertThat(execute(instructions, *tokens)).isTrue()
        tokens = arrayOf(
            token(GenericTokenType.IDENTIFIER),
            token(GenericTokenType.IDENTIFIER),
            token(GenericTokenType.LITERAL)
        )
        assertThat(execute(instructions, *tokens)).isFalse()
        tokens = arrayOf(
            token(GenericTokenType.IDENTIFIER),
            token(GenericTokenType.IDENTIFIER),
            token(GenericTokenType.LITERAL),
            token(GenericTokenType.LITERAL)
        )
        assertThat(execute(instructions, *tokens)).isTrue()
        tokens = arrayOf(
            token(GenericTokenType.IDENTIFIER),
            token(GenericTokenType.UNKNOWN_CHAR),
            token(GenericTokenType.LITERAL)
        )
        assertThat(execute(instructions, *tokens)).isTrue()
    }

    @Test
    fun tokenTypeClass() {
        val instructions = TokenTypeClassExpression(
            GenericTokenType::class.java
        ).compile(CompilationHandler())
        val tokens = arrayOf(token(GenericTokenType.IDENTIFIER))
        assertThat(execute(instructions, *tokens)).isTrue()
    }

    @Test
    fun adjacent() {
        val instructions = SequenceExpression(
            TokenValueExpression("foo"),
            AdjacentExpression.INSTANCE,
            TokenValueExpression("bar")
        ).compile(CompilationHandler())
        var tokens = arrayOf(token(1, 1, "foo"), token(1, 4, "bar"))
        assertThat(execute(instructions, *tokens)).isTrue()
        tokens = arrayOf(token(1, 1, "foo"), token(1, 5, "bar"))
        assertThat(execute(instructions, *tokens)).isFalse()
    }

    companion object {
        private fun token(type: TokenType): Token {
            return whenever(mock<Token>().type).thenReturn(type).getMock()
        }

        private fun token(value: String): Token {
            return whenever(mock<Token>().value).thenReturn(value).getMock()
        }

        private fun token(line: Int, column: Int, value: String): Token {
            val token = mock<Token>()
            whenever(token.line).thenReturn(line)
            whenever(token.column).thenReturn(column)
            whenever(token.value).thenReturn(value)
            return token
        }
    }
}