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
package com.sonar.sslr.impl.channel

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.test.lexer.LexerMatchers.hasToken
import com.sonar.sslr.test.lexer.MockHelper.mockLexer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.sonar.sslr.channel.CodeReader
import org.sonar.sslr.test.channel.ChannelMatchers.consume

class RegexpChannelTest {
    private var channel: RegexpChannel? = null
    private val lexer = mockLexer()
    @Test
    fun testRegexpToHandleNumber() {
        channel = RegexpChannel(GenericTokenType.CONSTANT, "[0-9]*")
        assertThat(channel, Matchers.not(consume("Not a number", lexer)))
        assertThat(channel, consume(CodeReader("56;"), lexer))
        assertThat(lexer.tokens, hasToken("56", GenericTokenType.CONSTANT))
    }

    @Test
    fun testColumnNumber() {
        channel = RegexpChannel(GenericTokenType.CONSTANT, "[0-9]*")
        assertThat(channel, consume("56;", lexer))
        assertEquals(lexer.tokens[0].column, 0)
    }
}