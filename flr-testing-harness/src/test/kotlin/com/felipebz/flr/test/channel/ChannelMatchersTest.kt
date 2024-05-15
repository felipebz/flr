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
package com.felipebz.flr.test.channel

import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChannelMatchersTest {
    @Test
    fun testConsumeMatcher() {
        val numberChannel: Channel<StringBuilder> = Channel { code, output ->
                if (Character.isDigit(code.peek())) {
                    output.append(code.pop().toChar())
                    return@Channel true
                }
                false
            }
        var output = StringBuilder()
        assertThat(numberChannel).consume("3", output)
        assertEquals(output.toString(), "3")
        assertThat(numberChannel).consume(CodeReader("333333"), output)
        output = StringBuilder()
        assertThat(numberChannel).doesNotConsume("n", output)
        assertEquals(output.toString(), "")
        assertThat(numberChannel).doesNotConsume(CodeReader("n"), output)
    }

    @Test
    fun testHasNextChar() {
        assertThat(CodeReader("123")).hasNextChar('1')
        assertThat(CodeReader("123")).doesNotHaveNextChar('n')
    }
}