/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.test.channel

import org.hamcrest.Matchers
import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Test
import org.sonar.sslr.channel.Channel
import org.sonar.sslr.channel.CodeReader

class ChannelMatchersTest {
    @Test
    fun testConsumeMatcher() {
        val numberChannel: Channel<StringBuilder> = object : Channel<StringBuilder>() {
            override fun consume(code: CodeReader, output: StringBuilder): Boolean {
                if (Character.isDigit(code.peek())) {
                    output.append(code.pop().toChar())
                    return true
                }
                return false
            }
        }
        var output = StringBuilder()
        Assert.assertThat(numberChannel, ChannelMatchers.consume("3", output))
        Assert.assertThat(output.toString(), Is.`is`("3"))
        Assert.assertThat(numberChannel, ChannelMatchers.consume(CodeReader("333333"), output))
        output = StringBuilder()
        Assert.assertThat(numberChannel, Matchers.not(ChannelMatchers.consume("n", output)))
        Assert.assertThat(output.toString(), Is.`is`(""))
        Assert.assertThat(numberChannel, Matchers.not(ChannelMatchers.consume(CodeReader("n"), output)))
    }

    @Test
    fun testHasNextChar() {
        Assert.assertThat(CodeReader("123"), ChannelMatchers.hasNextChar('1'))
        Assert.assertThat(CodeReader("123"), Matchers.not(ChannelMatchers.hasNextChar('n')))
    }
}