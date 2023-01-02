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
package com.felipebz.flr.channel

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.felipebz.flr.channel.ChannelDispatcher.Companion.builder

class RegexChannelTest {
    @Test
    fun shouldMatch() {
        val dispatcher = builder<StringBuilder>().addChannel(MyWordChannel()).addChannel(BlackholeChannel()).build()
        val output = StringBuilder()
        dispatcher.consume(CodeReader("my word"), output)
        assertEquals(output.toString(), "<w>my</w> <w>word</w>")
    }

    @Test
    fun shouldMatchTokenLongerThanBuffer() {
        val dispatcher = builder<StringBuilder>().addChannel(MyLiteralChannel()).build()
        val output = StringBuilder()
        val codeReaderConfiguration = CodeReaderConfiguration()
        val literalLength = 100000
        val veryLongLiteral = String.format(String.format("%%0%dd", literalLength), 0).replace("0", "a")
        assertEquals(veryLongLiteral.length, 100000)
        dispatcher.consume(CodeReader("\">$veryLongLiteral<\"", codeReaderConfiguration), output)
        assertEquals(output.toString(), "<literal>\">$veryLongLiteral<\"</literal>")
    }

    private class MyLiteralChannel : RegexChannel<StringBuilder>("\"[^\"]*+\"") {
        override fun consume(token: CharSequence?, output: StringBuilder) {
            output.append("<literal>$token</literal>")
        }
    }

    private class MyWordChannel : RegexChannel<StringBuilder>("\\w++") {
        override fun consume(token: CharSequence?, output: StringBuilder) {
            output.append("<w>$token</w>")
        }
    }

    private class BlackholeChannel : Channel<StringBuilder> {
        override fun consume(code: CodeReader, output: StringBuilder): Boolean {
            output.append(code.pop().toChar())
            return true
        }
    }
}