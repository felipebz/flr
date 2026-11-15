//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.internal.matchers.ImmutableInputBuffer
import com.felipebz.flr.internal.matchers.InputBuffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ParseErrorFormatterTest {
    private lateinit var formatter: ParseErrorFormatter

    @BeforeEach
    fun setUp() {
        formatter = ParseErrorFormatter()
    }

    @Test
    fun test() {
        val inputBuffer: InputBuffer = ImmutableInputBuffer("\t2+4*10-0*\n".toCharArray())
        val result = formatter.format(ParseError(inputBuffer, 10))
        print(result)
        val expected = StringBuilder()
            .append("Parse error at line 1 column 11:\n")
            .append('\n')
            .append("1:  2+4*10-0*\n")
            .append("             ^\n")
            .append("2: \n")
            .toString()
        assertThat(result).isEqualTo(expected)
    }
}
