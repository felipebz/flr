//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import org.assertj.core.api.Assertions.assertThat
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
