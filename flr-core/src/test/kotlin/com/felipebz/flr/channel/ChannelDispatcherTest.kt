/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
