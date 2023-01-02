/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.test.channel.ChannelMatchers.consume
import com.felipebz.flr.test.lexer.LexerMatchers.hasComment
import com.felipebz.flr.test.lexer.LexerMatchers.hasOriginalComment
import com.felipebz.flr.test.lexer.MockHelper.mockToken
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

class CommentChannelTest {
    private var channel: CommentRegexpChannel? = null
    private val output = LexerOutput()
    @Test
    fun testCommentRegexp() {
        channel = CommentRegexpChannel("//.*")
        assertThat(channel, Matchers.not(consume("This is not a comment", output)))
        assertThat(channel, consume("//My Comment\n second line", output))
        output.addToken(mockToken(GenericTokenType.EOF, "EOF"))
        assertThat(output.tokens, hasComment("//My Comment"))
        assertThat(output.tokens, hasOriginalComment("//My Comment"))
    }
}