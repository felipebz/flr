/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.Token
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        assertThat(LexerfulParseErrorFormatter().format(tokens, 1)).isEqualTo(expected)
    }

    companion object {
        private fun token(line: Int, column: Int, value: String): Token {
            val token = mock<Token>()
            whenever(token.line).thenReturn(line)
            whenever(token.column).thenReturn(column)
            whenever(token.originalValue).thenReturn(value)
            return token
        }
    }
}