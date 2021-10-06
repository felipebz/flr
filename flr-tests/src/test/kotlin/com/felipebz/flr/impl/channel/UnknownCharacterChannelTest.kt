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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.test.lexer.MockHelper.mockLexer
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import java.io.StringReader

class UnknownCharacterChannelTest {
    private val lexer = mockLexer()
    private val channel = UnknownCharacterChannel()
    @Test
    fun shouldConsumeAnyCharacter() {
        check("'", channel, GenericTokenType.UNKNOWN_CHAR, "'", mockLexer())
        check("a", channel, GenericTokenType.UNKNOWN_CHAR, "a", mockLexer())
    }

    @Test
    fun shouldConsumeEofCharacter() {
        assertThat(channel.consume(CodeReader(""), mockLexer())).isFalse()
    }

    private fun check(
        input: String,
        channel: Channel<Lexer>,
        expectedTokenType: TokenType,
        expectedTokenValue: String,
        lexer: Lexer
    ) {
        val code = CodeReader(StringReader(input))
        assertThat(channel.consume(code, lexer)).isTrue()
        assertEquals(lexer.tokens.size, 1)
        assertEquals(lexer.tokens[0].type, expectedTokenType)
        assertEquals(lexer.tokens[0].value, expectedTokenValue)
    }
}