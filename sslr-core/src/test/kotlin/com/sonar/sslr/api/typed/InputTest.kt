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
package com.sonar.sslr.api.typed

import org.fest.assertions.Assertions
import org.junit.Test
import java.io.File

class InputTest {
    @Test
    fun input() {
        val input = CharArray(0)
        Assertions.assertThat(Input(input).input()).isSameAs(input)
    }

    @Test
    fun uri() {
        val uri = File("tests://something").toURI()
        Assertions.assertThat(Input("".toCharArray(), uri).uri()).isSameAs(uri)
    }

    @Test
    fun substring() {
        val input = Input("abc".toCharArray())
        Assertions.assertThat(input.substring(0, 3)).isEqualTo("abc")
        Assertions.assertThat(input.substring(0, 2)).isEqualTo("ab")
        Assertions.assertThat(input.substring(0, 1)).isEqualTo("a")
        Assertions.assertThat(input.substring(0, 0)).isEqualTo("")
        Assertions.assertThat(input.substring(1, 3)).isEqualTo("bc")
        Assertions.assertThat(input.substring(2, 3)).isEqualTo("c")
        Assertions.assertThat(input.substring(3, 3)).isEqualTo("")
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
            Assertions.assertThat(location[0]).isEqualTo(expectedLine)
            Assertions.assertThat(location[1]).isEqualTo(expectedColumn)
        }
    }
}