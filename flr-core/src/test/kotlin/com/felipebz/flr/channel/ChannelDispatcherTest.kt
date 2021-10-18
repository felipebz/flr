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
package com.felipebz.flr.channel

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.felipebz.flr.channel.ChannelDispatcher.Companion.builder

class ChannelDispatcherTest {
    @Test
    fun shouldRemoveSpacesFromString() {
        val dispatcher = builder().addChannel(SpaceDeletionChannel()).build<StringBuilder>()
        val output = StringBuilder()
        dispatcher.consume(CodeReader("two words"), output)
        assertEquals(output.toString(), "twowords")
    }

    @Test
    fun shouldAddChannels() {
        val dispatcher = builder().addChannels(SpaceDeletionChannel(), FakeChannel()).build<StringBuilder>()
        assertEquals(dispatcher.getChannels().size, 2)
        MatcherAssert.assertThat(
            dispatcher.getChannels()[0], Matchers.instanceOf(SpaceDeletionChannel::class.java)
        )
        assertTrue(dispatcher.getChannels()[1] is FakeChannel)
    }

    @Test
    fun shouldThrowExceptionWhenNoChannelToConsumeNextCharacter() {
        assertThrows<IllegalStateException> {
            val dispatcher = builder().failIfNoChannelToConsumeOneCharacter().build<StringBuilder>()
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