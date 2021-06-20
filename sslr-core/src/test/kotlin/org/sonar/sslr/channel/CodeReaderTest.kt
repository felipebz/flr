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
package org.sonar.sslr.channel

import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Test
import java.io.StringReader
import java.util.regex.Pattern

class CodeReaderTest {
    @Test
    fun testPopWithAppendable() {
        val reader = CodeReader("package org.sonar;")
        val sw = StringBuilder()
        reader.pop(sw)
        Assert.assertEquals("p", sw.toString())
        reader.pop(sw)
        Assert.assertEquals("pa", sw.toString())
    }

    @Test
    fun testPeekACharArray() {
        val reader = CodeReader(StringReader("bar"))
        val chars = reader.peek(2)
        Assert.assertThat(chars.size, Is.`is`(2))
        Assert.assertThat(chars[0], Is.`is`('b'))
        Assert.assertThat(chars[1], Is.`is`('a'))
    }

    @Test
    fun testPeekTo() {
        val reader = CodeReader(StringReader("package org.sonar;"))
        val result = StringBuilder()
        reader.peekTo({ endFlag -> 'r' == endFlag.toChar() }, result)
        Assert.assertEquals("package o", result.toString())
        Assert.assertThat(reader.peek(), Is.`is`('p'.code)) // never called pop()
    }

    @Test
    fun peekTo_should_stop_at_end_of_input() {
        val reader = CodeReader("foo")
        val result = StringBuilder()
        reader.peekTo({ i -> false }, result)
        Assert.assertEquals("foo", result.toString())
    }

    @Test
    fun testPopToWithRegex() {
        val reader = CodeReader(StringReader("123ABC"))
        val token = StringBuilder()
        Assert.assertEquals(3, reader.popTo(Pattern.compile("\\d+").matcher(String()), token).toLong())
        Assert.assertEquals("123", token.toString())
        Assert.assertEquals(-1, reader.popTo(Pattern.compile("\\d+").matcher(String()), token).toLong())
        Assert.assertEquals(3, reader.popTo(Pattern.compile("\\w+").matcher(String()), token).toLong())
        Assert.assertEquals("123ABC", token.toString())
        Assert.assertEquals(-1, reader.popTo(Pattern.compile("\\w+").matcher(String()), token).toLong())

        // Should reset matcher with empty string:
        val matcher = Pattern.compile("\\d+").matcher("")
        reader.popTo(matcher, token)
        try {
            matcher.find(1)
            Assert.fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            Assert.assertEquals("Illegal start index", e.message)
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

        Assert.assertThrows(
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
        Assert.assertEquals(
            -1,
            CodeReader(StringReader("123 ABC")).popTo(digitMatcher, alphabeticMatcher, token).toLong()
        )
        Assert.assertEquals("", token.toString())
        Assert.assertEquals(
            3,
            CodeReader(StringReader("123ABC")).popTo(digitMatcher, alphabeticMatcher, token).toLong()
        )
        Assert.assertEquals("123", token.toString())
    }
}