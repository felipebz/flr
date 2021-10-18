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

import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.test.channel.ChannelMatchers.consume
import com.felipebz.flr.test.channel.ChannelMatchers.hasNextChar
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

class BlackHoleChannelTest {
    private val output = LexerOutput()
    private val channel = BlackHoleChannel("[ \\t]+")
    @Test
    fun testConsumeOneCharacter() {
        assertThat(channel, consume(" ", output))
        assertThat(channel, consume("\t", output))
        assertThat(channel, Matchers.not(consume("g", output)))
        assertThat(channel, Matchers.not(consume("-", output)))
        assertThat(channel, Matchers.not(consume("1", output)))
    }

    @Test
    fun consumeSeveralCharacters() {
        val reader = CodeReader("   \t123")
        assertThat(channel, consume(reader, output))
        assertThat(reader, hasNextChar('1'))
    }
}