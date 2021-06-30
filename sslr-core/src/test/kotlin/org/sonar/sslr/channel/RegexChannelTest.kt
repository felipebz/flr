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
package org.sonar.sslr.channel

import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.sonar.sslr.channel.ChannelDispatcher.Companion.builder

class RegexChannelTest {
    @Test
    fun shouldMatch() {
        val dispatcher = builder().addChannel(MyWordChannel()).addChannel(BlackholeChannel()).build<StringBuilder>()
        val output = StringBuilder()
        dispatcher.consume(CodeReader("my word"), output)
        Assert.assertThat(output.toString(), Matchers.`is`("<w>my</w> <w>word</w>"))
    }

    @Test
    fun shouldMatchTokenLongerThanBuffer() {
        val dispatcher = builder().addChannel(MyLiteralChannel()).build<StringBuilder>()
        val output = StringBuilder()
        val codeReaderConfiguration = CodeReaderConfiguration()
        val literalLength = 100000
        val veryLongLiteral = String.format(String.format("%%0%dd", literalLength), 0).replace("0", "a")
        Assert.assertThat(veryLongLiteral.length, Matchers.`is`(100000))
        dispatcher.consume(CodeReader("\">$veryLongLiteral<\"", codeReaderConfiguration), output)
        Assert.assertThat(output.toString(), Matchers.`is`("<literal>\">$veryLongLiteral<\"</literal>"))
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

    private class BlackholeChannel : Channel<StringBuilder>() {
        override fun consume(code: CodeReader, output: StringBuilder): Boolean {
            output.append(code.pop().toChar())
            return true
        }
    }
}