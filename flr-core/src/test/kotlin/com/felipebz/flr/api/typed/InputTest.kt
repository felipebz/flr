//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api.typed

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InputTest {
    @Test
    fun input() {
        val input = CharArray(0)
        assertThat(Input(input).input()).isSameAs(input)
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
