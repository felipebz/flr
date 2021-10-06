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
package com.felipebz.flr.test.channel

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader

class ChannelMatchersTest {
    @Test
    fun testConsumeMatcher() {
        val numberChannel: Channel<StringBuilder> = object :
            Channel<StringBuilder> {
            override fun consume(code: CodeReader, output: StringBuilder): Boolean {
                if (Character.isDigit(code.peek())) {
                    output.append(code.pop().toChar())
                    return true
                }
                return false
            }
        }
        var output = StringBuilder()
        assertThat(numberChannel, ChannelMatchers.consume("3", output))
        assertEquals(output.toString(), "3")
        assertThat(numberChannel, ChannelMatchers.consume(CodeReader("333333"), output))
        output = StringBuilder()
        assertThat(numberChannel, Matchers.not(ChannelMatchers.consume("n", output)))
        assertEquals(output.toString(), "")
        assertThat(numberChannel, Matchers.not(ChannelMatchers.consume(CodeReader("n"), output)))
    }

    @Test
    fun testHasNextChar() {
        assertThat(CodeReader("123"), ChannelMatchers.hasNextChar('1'))
        assertThat(CodeReader("123"), Matchers.not(ChannelMatchers.hasNextChar('n')))
    }
}