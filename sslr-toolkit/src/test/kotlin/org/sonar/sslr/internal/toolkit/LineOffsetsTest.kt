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
package org.sonar.sslr.internal.toolkit

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import java.net.URI
import java.net.URISyntaxException

class LineOffsetsTest {
    @Test
    fun startOffset() {
        val foo = mockToken(1, 0, "foo")
        val bar = mockToken(2, 2, "bar")
        val lineOffsets = LineOffsets("foo\n??bar")
        Assertions.assertThat(lineOffsets.getStartOffset(foo)).isEqualTo(0)
        Assertions.assertThat(lineOffsets.getStartOffset(bar)).isEqualTo(6)
    }

    @Test
    fun endOffsetSingleLine() {
        val foo = mockToken(1, 0, "foo")
        val bar = mockToken(2, 2, "bar")
        val lineOffsets = LineOffsets("foo\n??bar...")
        Assertions.assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3)
        Assertions.assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(9)
    }

    @Test
    fun endOffsetMultiLine() {
        val foo = mockToken(1, 0, "foo")
        val bar = mockToken(2, 2, "bar\nbaz")
        val lineOffsets = LineOffsets("foo\n??bar\nbaz...")
        Assertions.assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3)
        Assertions.assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(13)
    }

    @Test
    fun endOffsetMultiLineRNSingleOffsetIncrement() {
        val foo = mockToken(1, 0, "foo")
        val bar = mockToken(2, 2, "bar\r\nbaz")
        val lineOffsets = LineOffsets("foo\n??bar\r\nbaz...")
        Assertions.assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3)
        Assertions.assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(13)
    }

    @Test
    fun endOffsetMultiLineRNewLine() {
        val foo = mockToken(1, 0, "foo")
        val bar = mockToken(2, 2, "bar\rbaz")
        val lineOffsets = LineOffsets("foo\n??bar\rbaz...")
        Assertions.assertThat(lineOffsets.getEndOffset(foo)).isEqualTo(3)
        Assertions.assertThat(lineOffsets.getEndOffset(bar)).isEqualTo(13)
    }

    @Test
    fun offset() {
        val lineOffsets = LineOffsets("int a = 0;\nint b = 0;")
        Assertions.assertThat(lineOffsets.getOffset(2, 4)).isEqualTo(15)
        Assertions.assertThat(lineOffsets.getOffset(2, 100)).isEqualTo(21)
        Assertions.assertThat(lineOffsets.getOffset(100, 100)).isEqualTo(21)
    }

    @Test
    fun offsetCariageReturnAsNewLine() {
        val lineOffsets = LineOffsets("\rfoo")
        Assertions.assertThat(lineOffsets.getOffset(1, 0)).isEqualTo(0)
        Assertions.assertThat(lineOffsets.getOffset(2, 0)).isEqualTo(1)
    }

    @Test
    fun offsetCariageReturnAndLineFeedAsSingleOffset() {
        val lineOffsets = LineOffsets("\r\nfoo")
        Assertions.assertThat(lineOffsets.getOffset(1, 0)).isEqualTo(0)
        Assertions.assertThat(lineOffsets.getOffset(2, 0)).isEqualTo(1)
    }

    @Test
    fun offsetBadLine() {
        assertThrows(IllegalArgumentException::class.java) {
            val lineOffsets = LineOffsets("")
            lineOffsets.getOffset(0, 0)
        }
    }

    @Test
    fun offsetBadColumn() {
        assertThrows(IllegalArgumentException::class.java) {
            val lineOffsets = LineOffsets("")
            lineOffsets.getOffset(1, -1)
        }
    }

    companion object {
        fun mockToken(line: Int, column: Int, value: String?): Token {
            return try {
                Token.builder()
                    .setLine(line)
                    .setColumn(column)
                    .setValueAndOriginalValue(value!!)
                    .setType(GenericTokenType.IDENTIFIER)
                    .setURI(URI("tests://unittest"))
                    .build()
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }
    }
}