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
package com.sonar.sslr.api.typed

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import java.io.File

class InputTest {
    @Test
    fun input() {
        val input = CharArray(0)
        assertThat(Input(input).input()).isSameAs(input)
    }

    @Test
    fun uri() {
        val uri = File("tests://something").toURI()
        assertThat(Input("".toCharArray(), uri).uri()).isSameAs(uri)
    }

    @Test
    fun substring() {
        val input = Input("abc".toCharArray())
        assertThat(input.substring(0, 3)).isEqualTo("abc")
        assertThat(input.substring(0, 2)).isEqualTo("ab")
        assertThat(input.substring(0, 1)).isEqualTo("a")
        assertThat(input.substring(0, 0)).isEqualTo("")
        assertThat(input.substring(1, 3)).isEqualTo("bc")
        assertThat(input.substring(2, 3)).isEqualTo("c")
        assertThat(input.substring(3, 3)).isEqualTo("")
    }

    @Test
    fun lineAndColumnAt() {
        assertLineAndColumn(
            "", 0,
            1, 1
        )
        assertLineAndColumn(
            "abc", 0,
            1, 1
        )
        assertLineAndColumn(
            "abc", 1,
            1, 2
        )
        assertLineAndColumn(
            "abc", 2,
            1, 3
        )
        assertLineAndColumn(
            "\n_", 1,
            2, 1
        )
        assertLineAndColumn(
            "\r_", 1,
            2, 1
        )
        assertLineAndColumn(
            "\r\n_", 2,
            2, 1
        )
        assertLineAndColumn(
            "\r", 1,
            2, 1
        )
    }

    companion object {
        private fun assertLineAndColumn(string: String, index: Int, expectedLine: Int, expectedColumn: Int) {
            val location = Input(string.toCharArray()).lineAndColumnAt(index)
            assertThat(location[0]).isEqualTo(expectedLine)
            assertThat(location[1]).isEqualTo(expectedColumn)
        }
    }
}