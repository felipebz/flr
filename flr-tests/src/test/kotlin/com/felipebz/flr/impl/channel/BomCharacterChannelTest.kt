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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test

class BomCharacterChannelTest {
    private val output = LexerOutput()
    private val channel = BomCharacterChannel()
    @Test
    fun shouldConsumeBomCharacter() {
        assertThat(channel.consume(CodeReader("\uFEFF"), output)).isTrue()
        assertThat(output.tokens.size).isEqualTo(0)
    }

    @Test
    fun shouldNotConsumeOtherCharacters() {
        assertThat(channel.consume(CodeReader(" "), output)).isFalse()
        assertThat(output.tokens.size).isEqualTo(0)
    }
}