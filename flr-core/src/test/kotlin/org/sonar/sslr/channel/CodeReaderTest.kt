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
package org.sonar.sslr.channel

import org.junit.Assert.*
import org.junit.Test
import java.io.StringReader
import java.util.regex.Pattern

class CodeReaderTest {
    @Test
    fun testPopWithAppendable() {
        val reader = CodeReader("package org.sonar;")
        val sw = StringBuilder()
        reader.pop(sw)
        assertEquals("p", sw.toString())
        reader.pop(sw)
        assertEquals("pa", sw.toString())
    }

    @Test
    fun testPeekACharArray() {
        val reader = CodeReader(StringReader("bar"))
        val chars = reader.peek(2)
        assertEquals(chars.size, 2)
        assertEquals(chars[0], 'b')
        assertEquals(chars[1], 'a')
    }

    @Test
    fun testPeekTo() {
        val reader = CodeReader(StringReader("package org.sonar;"))
        val result = StringBuilder()
        reader.peekTo({ endFlag -> 'r' == endFlag.toChar() }, result)
        assertEquals("package o", result.toString())
        assertEquals(reader.peek(), 'p'.code) // never called pop()
    }

    @Test
    fun peekTo_should_stop_at_end_of_input() {
        val reader = CodeReader("foo")
        val result = StringBuilder()
        reader.peekTo({ i -> false }, result)
        assertEquals("foo", result.toString())
    }

    @Test
    fun testPopToWithRegex() {
        val reader = CodeReader(StringReader("123ABC"))
        val token = StringBuilder()
        assertEquals(3, reader.popTo(Pattern.compile("\\d+").matcher(String()), token).toLong())
        assertEquals("123", token.toString())
        assertEquals(-1, reader.popTo(Pattern.compile("\\d+").matcher(String()), token).toLong())
        assertEquals(3, reader.popTo(Pattern.compile("\\w+").matcher(String()), token).toLong())
        assertEquals("123ABC", token.toString())
        assertEquals(-1, reader.popTo(Pattern.compile("\\w+").matcher(String()), token).toLong())

        // Should reset matcher with empty string:
        val matcher = Pattern.compile("\\d+").matcher("")
        reader.popTo(matcher, token)
        try {
            matcher.find(1)
            fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            assertEquals("Illegal start index", e.message)
        }
    }

    @Test
    fun testStackOverflowError() {
        val sb = StringBuilder()
        sb.append("\n")
        for (i in 0..9999) {
            sb.append(Integer.toHexString(i))
        }
        val reader = CodeReader(sb.toString())
        reader.pop()
        reader.pop()

        assertThrows(
            "Unable to apply regular expression '([a-fA-F]|\\d)+' at line 2 and column 1," +
                    " because it led to a stack overflow error." +
                    " This error may be due to an inefficient use of alternations - see https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507",
            ChannelException::class.java
        ) {
            reader.popTo(Pattern.compile("([a-fA-F]|\\d)+").matcher(""), StringBuilder())
        }
    }

    @Test
    fun testPopToWithRegexAndFollowingMatcher() {
        val digitMatcher = Pattern.compile("\\d+").matcher(String())
        val alphabeticMatcher = Pattern.compile("[a-zA-Z]").matcher(String())
        val token = StringBuilder()
        assertEquals(
            -1,
            CodeReader(StringReader("123 ABC")).popTo(digitMatcher, alphabeticMatcher, token).toLong()
        )
        assertEquals("", token.toString())
        assertEquals(
            3,
            CodeReader(StringReader("123ABC")).popTo(digitMatcher, alphabeticMatcher, token).toLong()
        )
        assertEquals("123", token.toString())
    }
}