/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.sslr.internal.matchers

import org.fest.assertions.Assertions
import org.junit.Test

class ImmutableInputBufferTest {
    @Test
    fun test() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("foo\r\nbar\nbaz\rqux\r".toCharArray())
        Assertions.assertThat(inputBuffer.getLineCount()).isEqualTo(5)
        Assertions.assertThat(inputBuffer.extractLine(1)).isEqualTo("foo\r\n")
        Assertions.assertThat(inputBuffer.extractLine(2)).isEqualTo("bar\n")
        Assertions.assertThat(inputBuffer.extractLine(3)).isEqualTo("baz\r")
        Assertions.assertThat(inputBuffer.extractLine(4)).isEqualTo("qux\r")
        Assertions.assertThat(inputBuffer.extractLine(5)).isEqualTo("")
        Assertions.assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(inputBuffer.getPosition(4)).isEqualTo(InputBuffer.Position(1, 5))
        Assertions.assertThat(inputBuffer.getPosition(5)).isEqualTo(InputBuffer.Position(2, 1))
        Assertions.assertThat(inputBuffer.getPosition(8)).isEqualTo(InputBuffer.Position(2, 4))
        Assertions.assertThat(inputBuffer.getPosition(9)).isEqualTo(InputBuffer.Position(3, 1))
        Assertions.assertThat(inputBuffer.getPosition(12)).isEqualTo(InputBuffer.Position(3, 4))
        Assertions.assertThat(inputBuffer.getPosition(13)).isEqualTo(InputBuffer.Position(4, 1))
        Assertions.assertThat(inputBuffer.getPosition(16)).isEqualTo(InputBuffer.Position(4, 4))
        Assertions.assertThat(inputBuffer.getPosition(17)).isEqualTo(InputBuffer.Position(5, 1))
    }

    @Test
    fun test_single_line() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("foo".toCharArray())
        Assertions.assertThat(inputBuffer.getLineCount()).isEqualTo(1)
        Assertions.assertThat(inputBuffer.extractLine(1)).isEqualTo("foo")
        Assertions.assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(1, 2))
        Assertions.assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(1, 3))
        Assertions.assertThat(inputBuffer.getPosition(3)).isEqualTo(InputBuffer.Position(1, 4))
    }

    @Test
    fun test_empty() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("".toCharArray())
        Assertions.assertThat(inputBuffer.getLineCount()).isEqualTo(1)
        Assertions.assertThat(inputBuffer.extractLine(1)).isEqualTo("")
        Assertions.assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(1, 2))
    }

    @Test
    fun test_empty_lines_with_LF() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\n\n".toCharArray())
        Assertions.assertThat(inputBuffer.getLineCount()).isEqualTo(3)
        Assertions.assertThat(inputBuffer.extractLine(1)).isEqualTo("\n")
        Assertions.assertThat(inputBuffer.extractLine(2)).isEqualTo("\n")
        Assertions.assertThat(inputBuffer.extractLine(3)).isEqualTo("")
        Assertions.assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(2, 1))
        Assertions.assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(3, 1))
    }

    @Test
    fun test_empty_lines_with_CR() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\r\r".toCharArray())
        Assertions.assertThat(inputBuffer.getLineCount()).isEqualTo(3)
        Assertions.assertThat(inputBuffer.extractLine(1)).isEqualTo("\r")
        Assertions.assertThat(inputBuffer.extractLine(2)).isEqualTo("\r")
        Assertions.assertThat(inputBuffer.extractLine(3)).isEqualTo("")
        Assertions.assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(2, 1))
        Assertions.assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(3, 1))
    }

    @Test
    fun test_empty_lines_with_CRLF() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\r\n\r\n".toCharArray())
        Assertions.assertThat(inputBuffer.getLineCount()).isEqualTo(3)
        Assertions.assertThat(inputBuffer.extractLine(1)).isEqualTo("\r\n")
        Assertions.assertThat(inputBuffer.extractLine(2)).isEqualTo("\r\n")
        Assertions.assertThat(inputBuffer.extractLine(3)).isEqualTo("")
        Assertions.assertThat(inputBuffer.getPosition(0)).isEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(inputBuffer.getPosition(1)).isEqualTo(InputBuffer.Position(1, 2))
        Assertions.assertThat(inputBuffer.getPosition(2)).isEqualTo(InputBuffer.Position(2, 1))
        Assertions.assertThat(inputBuffer.getPosition(3)).isEqualTo(InputBuffer.Position(2, 2))
        Assertions.assertThat(inputBuffer.getPosition(4)).isEqualTo(InputBuffer.Position(3, 1))
    }

    @Test
    fun test_equality_and_hash_code_of_positions() {
        val position = InputBuffer.Position(0, 0)
        Assertions.assertThat(position).isEqualTo(position)
        Assertions.assertThat(position).isEqualTo(InputBuffer.Position(0, 0))
        Assertions.assertThat(position.hashCode()).isEqualTo(InputBuffer.Position(0, 0).hashCode())
        Assertions.assertThat(position).isNotEqualTo(InputBuffer.Position(0, 1))
        Assertions.assertThat(position).isNotEqualTo(InputBuffer.Position(1, 1))
        Assertions.assertThat(position).isNotEqualTo(Any())
    }
}