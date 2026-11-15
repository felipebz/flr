//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class BlackHoleChannelTest {
    private val output = LexerOutput()
    private val channel = BlackHoleChannel("[ \\t]+")
    @Test
    fun testConsumeOneCharacter() {
        assertThat(channel).consume(" ", output)
        assertThat(channel).consume("\t", output)
        assertThat(channel).doesNotConsume("g", output)
        assertThat(channel).doesNotConsume("-", output)
        assertThat(channel).doesNotConsume("1", output)
    }

    @Test
    fun consumeSeveralCharacters() {
        val reader = CodeReader("   \t123")
        assertThat(channel).consume(reader, output)
        assertThat(reader).hasNextChar('1')
    }
}
