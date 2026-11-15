//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
