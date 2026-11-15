//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.channel

import com.felipebz.flr.channel.ChannelDispatcher.Companion.builder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
