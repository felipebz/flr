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
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.StringReader

class UnknownCharacterChannelTest {
    private val channel = UnknownCharacterChannel()
    @Test
    fun shouldConsumeAnyCharacter() {
        check("'", channel, GenericTokenType.UNKNOWN_CHAR, "'", LexerOutput())
        check("a", channel, GenericTokenType.UNKNOWN_CHAR, "a", LexerOutput())
    }

    @Test
    fun shouldConsumeEofCharacter() {
        assertThat(channel.consume(CodeReader(""), LexerOutput())).isFalse()
    }

    private fun check(
        input: String,
        channel: Channel<LexerOutput>,
        expectedTokenType: TokenType,
        expectedTokenValue: String,
        output: LexerOutput
    ) {
        val code = CodeReader(StringReader(input))
        assertThat(channel.consume(code, output)).isTrue()
        assertEquals(output.tokens.size, 1)
        assertEquals(output.tokens[0].type, expectedTokenType)
        assertEquals(output.tokens[0].value, expectedTokenValue)
    }
}