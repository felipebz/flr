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
package org.sonar.sslr.internal.matchers

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test

class ImmutableInputBufferTest {
    @Test
    fun test() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("foo\r\nbar\nbaz\rqux\r".toCharArray())
        assertThat(inputBuffer.getLineCount()).isEqualTo(5)
        assertThat(inputBuffer.extractLine(1)).isEqualTo("foo\r\n")
        assertThat(inputBuffer.extractLine(2)).isEqualTo("bar\n")
        assertThat(inputBuffer.extractLine(3)).isEqualTo("baz\r")
        assertThat(inputBuffer.extractLine(4)).isEqualTo("qux\r")
        assertThat(inputBuffer.extractLine(5)).isEqualTo("")
        assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        assertThat(inputBuffer.getPosition(4)).isEqualTo(InputBuffer.Position(1, 5))
        assertThat(inputBuffer.getPosition(5)).isEqualTo(InputBuffer.Position(2, 1))
        assertThat(inputBuffer.getPosition(8)).isEqualTo(InputBuffer.Position(2, 4))
        assertThat(inputBuffer.getPosition(9)).isEqualTo(InputBuffer.Position(3, 1))
        assertThat(inputBuffer.getPosition(12)).isEqualTo(InputBuffer.Position(3, 4))
        assertThat(inputBuffer.getPosition(13)).isEqualTo(InputBuffer.Position(4, 1))
        assertThat(inputBuffer.getPosition(16)).isEqualTo(InputBuffer.Position(4, 4))
        assertThat(inputBuffer.getPosition(17)).isEqualTo(InputBuffer.Position(5, 1))
    }

    @Test
    fun test_single_line() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("foo".toCharArray())
        assertThat(inputBuffer.getLineCount()).isEqualTo(1)
        assertThat(inputBuffer.extractLine(1)).isEqualTo("foo")
        assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(1, 2))
        assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(1, 3))
        assertThat(inputBuffer.getPosition(3)).isEqualTo(InputBuffer.Position(1, 4))
    }

    @Test
    fun test_empty() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("".toCharArray())
        assertThat(inputBuffer.getLineCount()).isEqualTo(1)
        assertThat(inputBuffer.extractLine(1)).isEqualTo("")
        assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(1, 2))
    }

    @Test
    fun test_empty_lines_with_LF() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\n\n".toCharArray())
        assertThat(inputBuffer.getLineCount()).isEqualTo(3)
        assertThat(inputBuffer.extractLine(1)).isEqualTo("\n")
        assertThat(inputBuffer.extractLine(2)).isEqualTo("\n")
        assertThat(inputBuffer.extractLine(3)).isEqualTo("")
        assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(2, 1))
        assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(3, 1))
    }

    @Test
    fun test_empty_lines_with_CR() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\r\r".toCharArray())
        assertThat(inputBuffer.getLineCount()).isEqualTo(3)
        assertThat(inputBuffer.extractLine(1)).isEqualTo("\r")
        assertThat(inputBuffer.extractLine(2)).isEqualTo("\r")
        assertThat(inputBuffer.extractLine(3)).isEqualTo("")
        assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(2, 1))
        assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(3, 1))
    }

    @Test
    fun test_empty_lines_with_CRLF() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\r\n\r\n".toCharArray())
        assertThat(inputBuffer.getLineCount()).isEqualTo(3)
        assertThat(inputBuffer.extractLine(1)).isEqualTo("\r\n")
        assertThat(inputBuffer.extractLine(2)).isEqualTo("\r\n")
        assertThat(inputBuffer.extractLine(3)).isEqualTo("")
        assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(1, 2))
        assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(2, 1))
        assertThat(inputBuffer.getPosition(3)).isEqualTo(InputBuffer.Position(2, 2))
        assertThat(inputBuffer.getPosition(4)).isEqualTo(InputBuffer.Position(3, 1))
    }

    @Test
    fun test_equality_and_hash_code_of_positions() {
        val position = InputBuffer.Position(0, 0)
        assertThat(position).isEqualTo(position)
        assertThat(position).isEqualTo(InputBuffer.Position(0, 0))
        assertThat(position.hashCode()).isEqualTo(InputBuffer.Position(0, 0).hashCode())
        assertThat(position).isNotEqualTo(InputBuffer.Position(0, 1))
        assertThat(position).isNotEqualTo(InputBuffer.Position(1, 1))
        assertThat(position).isNotEqualTo(Any())
    }
}