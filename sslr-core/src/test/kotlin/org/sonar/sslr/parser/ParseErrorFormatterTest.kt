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
package org.sonar.sslr.parser

import org.fest.assertions.Assertions
import org.junit.Before
import org.junit.Test
import org.sonar.sslr.internal.matchers.ImmutableInputBuffer
import org.sonar.sslr.internal.matchers.InputBuffer

class ParseErrorFormatterTest {
    private lateinit var formatter: ParseErrorFormatter

    @Before
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
        Assertions.assertThat(result).isEqualTo(expected)
    }
}