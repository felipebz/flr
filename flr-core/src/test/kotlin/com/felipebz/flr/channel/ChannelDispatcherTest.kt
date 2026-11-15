//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.channel

import com.felipebz.flr.channel.ChannelDispatcher.Companion.builder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChannelDispatcherTest {
    @Test
    fun shouldRemoveSpacesFromString() {
        val dispatcher = builder<StringBuilder>().addChannel(SpaceDeletionChannel()).build()
        val output = StringBuilder()
        dispatcher.consume(CodeReader("two words"), output)
        assertEquals(output.toString(), "twowords")
    }

    @Test
    fun shouldAddChannels() {
        val dispatcher = builder<StringBuilder>().addChannels(SpaceDeletionChannel(), FakeChannel()).build()
        assertEquals(dispatcher.channels.size, 2)
        assertThat(dispatcher.channels[0]).isInstanceOf(SpaceDeletionChannel::class.java)
        assertTrue(dispatcher.channels[1] is FakeChannel)
    }

    @Test
    fun shouldThrowExceptionWhenNoChannelToConsumeNextCharacter() {
        assertThrows<IllegalStateException> {
            val dispatcher = builder<StringBuilder>().failIfNoChannelToConsumeOneCharacter().build()
            dispatcher.consume(CodeReader("two words"), StringBuilder())
        }
    }

    private class SpaceDeletionChannel : Channel<StringBuilder> {
        override fun consume(code: CodeReader, output: StringBuilder): Boolean {
            if (code.peek() == ' '.code) {
                code.pop()
            } else {
                output.append(code.pop().toChar())
            }
            return true
        }
    }

    private class FakeChannel : Channel<StringBuilder> {
        override fun consume(code: CodeReader, output: StringBuilder): Boolean {
            return true
        }
    }
}
