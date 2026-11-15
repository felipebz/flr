//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.Token
import org.assertj.core.api.Assertions.assertThat
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
            .append("             ^ error location\n")
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
