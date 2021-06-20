/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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

import com.sonar.sslr.api.Token
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito

class LexerfulParseErrorFormatterTest {
    @Test
    fun test() {
        val tokens = listOf(
            token(2, 1, "foo\nbar\nbaz"),
            token(4, 6, "qux"),
            token(6, 3, "end")
        )
        val expected = StringBuilder()
            .append("Parse error at line 4 column 6:\n")
            .append("\n")
            .append("    2: foo\n")
            .append("    3: bar\n")
            .append("  -->  baz   qux\n")
            .append("    5: \n")
            .append("    6:    end\n")
            .toString()
        Assertions.assertThat(LexerfulParseErrorFormatter().format(tokens, 1)).isEqualTo(expected)
    }

    companion object {
        private fun token(line: Int, column: Int, value: String): Token {
            val token = Mockito.mock(Token::class.java)
            Mockito.`when`(token.line).thenReturn(line)
            Mockito.`when`(token.column).thenReturn(column)
            Mockito.`when`(token.originalValue).thenReturn(value)
            return token
        }
    }
}