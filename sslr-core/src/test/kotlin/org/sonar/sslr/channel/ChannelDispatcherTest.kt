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

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.sonar.sslr.channel.ChannelDispatcher.Companion.builder

class ChannelDispatcherTest {
    @Test
    fun shouldRemoveSpacesFromString() {
        val dispatcher = builder().addChannel(SpaceDeletionChannel()).build<StringBuilder>()
        val output = StringBuilder()
        dispatcher.consume(CodeReader("two words"), output)
        Assert.assertThat(output.toString(), Matchers.`is`("twowords"))
    }

    @Test
    fun shouldAddChannels() {
        val dispatcher = builder().addChannels(SpaceDeletionChannel(), FakeChannel()).build<StringBuilder>()
        Assert.assertThat(dispatcher.getChannels().size, Matchers.`is`(2))
        MatcherAssert.assertThat(
            dispatcher.getChannels()[0], Matchers.instanceOf(SpaceDeletionChannel::class.java)
        )
        Assert.assertThat(dispatcher.getChannels()[1], Matchers.instanceOf(FakeChannel::class.java))
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowExceptionWhenNoChannelToConsumeNextCharacter() {
        val dispatcher = builder().failIfNoChannelToConsumeOneCharacter().build<StringBuilder>()
        dispatcher.consume(CodeReader("two words"), StringBuilder())
    }

    private class SpaceDeletionChannel : Channel<StringBuilder>() {
        override fun consume(code: CodeReader, output: StringBuilder): Boolean {
            if (code.peek() == ' '.code) {
                code.pop()
            } else {
                output.append(code.pop().toChar())
            }
            return true
        }
    }

    private class FakeChannel : Channel<StringBuilder>() {
        override fun consume(code: CodeReader, output: StringBuilder): Boolean {
            return true
        }
    }
}