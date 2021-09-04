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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.io.IOException
import java.time.Duration
import java.time.Duration.ofSeconds
import java.util.regex.Pattern

class CodeBufferTest {
    private val defaulConfiguration = CodeReaderConfiguration()
    @Test
    fun testPop() {
        val code = CodeBuffer("pa", defaulConfiguration)
        assertEquals(code.pop().toChar(), 'p')
        assertEquals(code.pop().toChar(), 'a')
        assertEquals(code.pop(), -1)
    }

    @Test
    fun testPeek() {
        val code = CodeBuffer("pa", defaulConfiguration)
        assertEquals(code.peek().toChar(), 'p')
        assertEquals(code.peek().toChar(), 'p')
        code.pop()
        assertEquals(code.peek().toChar(), 'a')
        code.pop()
        assertEquals(code.peek(), -1)
    }

    @Test
    fun testLastCharacter() {
        val reader = CodeBuffer("bar", defaulConfiguration)
        assertEquals(reader.lastChar(), -1)
        reader.pop()
        assertEquals(reader.lastChar().toChar(), 'b')
    }

    @Test
    fun testGetColumnAndLinePosition() {
        val reader = CodeBuffer("pa\nc\r\ns\r\n\r\n", defaulConfiguration)
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 1)
        reader.pop() // p
        reader.pop() // a
        assertEquals(reader.getColumnPosition(), 2)
        assertEquals(reader.getLinePosition(), 1)
        reader.peek() // \n
        reader.lastChar() // a
        assertEquals(reader.getColumnPosition(), 2)
        assertEquals(reader.getLinePosition(), 1)
        reader.pop() // \n
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 2)
        reader.pop() // c
        assertEquals(reader.getColumnPosition(), 1)
        assertEquals(reader.getLinePosition(), 2)
        reader.pop() // \r
        reader.pop() // \n
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 3)
        assertEquals(reader.pop().toChar(), 's')
        reader.pop() // \r
        assertEquals(reader.getColumnPosition(), 2)
        assertEquals(reader.getLinePosition(), 3)
        reader.pop() // \n
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 4)
        reader.pop() // \r
        reader.pop() // \n
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 5)
    }

    @Test
    fun testStartAndStopRecording() {
        val reader = CodeBuffer("123456", defaulConfiguration)
        reader.pop()
        assertEquals("", reader.stopRecording().toString())
        reader.startRecording()
        reader.pop()
        reader.pop()
        reader.peek()
        assertEquals("23", reader.stopRecording().toString())
        assertEquals("", reader.stopRecording().toString())
    }

    @Test
    fun testCharAt() {
        val reader = CodeBuffer("123456", defaulConfiguration)
        assertEquals('1', reader[0])
        assertEquals('6', reader[5])
    }

    @Test
    fun testCharAtIndexOutOfBoundsException() {
        val reader = CodeBuffer("12345", defaulConfiguration)
        assertEquals(reader[5], (-1).toChar())
    }

    @Test
    fun testReadWithSpecificTabWidth() {
        val configuration = CodeReaderConfiguration()
        configuration.setTabWidth(4)
        val reader = CodeBuffer("pa\n\tc", configuration)
        assertEquals('\n', reader[2])
        assertEquals('\t', reader[3])
        assertEquals('c', reader[4])
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 1)
        reader.pop() // p
        reader.pop() // a
        assertEquals(reader.getColumnPosition(), 2)
        assertEquals(reader.getLinePosition(), 1)
        reader.peek() // \n
        reader.lastChar() // a
        assertEquals(reader.getColumnPosition(), 2)
        assertEquals(reader.getLinePosition(), 1)
        reader.pop() // \n
        assertEquals(reader.getColumnPosition(), 0)
        assertEquals(reader.getLinePosition(), 2)
        reader.pop() // \t
        assertEquals(reader.getColumnPosition(), 4)
        assertEquals(reader.getLinePosition(), 2)
        reader.pop() // c
        assertEquals(reader.getColumnPosition(), 5)
        assertEquals(reader.getLinePosition(), 2)
    }

    @Test
    fun testCodeReaderFilter() {
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(ReplaceNumbersFilter())
        val code = CodeBuffer("abcd12efgh34", configuration)
        // test #charAt
        assertEquals('a', code[0])
        assertEquals('-', code[4])
        assertEquals('-', code[5])
        assertEquals('e', code[6])
        assertEquals('-', code[10])
        assertEquals('-', code[11])
        // test peek and pop
        assertEquals(code.peek().toChar(), 'a')
        assertEquals(code.pop().toChar(), 'a')
        assertEquals(code.pop().toChar(), 'b')
        assertEquals(code.pop().toChar(), 'c')
        assertEquals(code.pop().toChar(), 'd')
        assertEquals(code.peek().toChar(), '-')
        assertEquals(code.pop().toChar(), '-')
        assertEquals(code.pop().toChar(), '-')
        assertEquals(code.pop().toChar(), 'e')
        assertEquals(code.pop().toChar(), 'f')
        assertEquals(code.pop().toChar(), 'g')
        assertEquals(code.pop().toChar(), 'h')
        assertEquals(code.pop().toChar(), '-')
        assertEquals(code.pop().toChar(), '-')
    }

    @Test
    fun theLengthShouldBeTheSameThanTheStringLength() {
        val myCode = "myCode"
        assertEquals(CodeBuffer(myCode, CodeReaderConfiguration()).length, 6)
    }

    @Test
    fun theLengthShouldDecreaseEachTimeTheInputStreamIsConsumed() {
        val myCode = "myCode"
        val codeBuffer = CodeBuffer(myCode, CodeReaderConfiguration())
        codeBuffer.pop()
        codeBuffer.pop()
        assertEquals(codeBuffer.length, 4)
    }

    @Test
    fun testSeveralCodeReaderFilter() {
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(ReplaceNumbersFilter(), ReplaceCharFilter())
        val code = CodeBuffer("abcd12efgh34", configuration)
        // test #charAt
        assertEquals('*', code[0])
        assertEquals('-', code[4])
        assertEquals('-', code[5])
        assertEquals('*', code[6])
        assertEquals('-', code[10])
        assertEquals('-', code[11])
        // test peek and pop
        assertEquals(code.peek().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.peek().toChar(), '-')
        assertEquals(code.pop().toChar(), '-')
        assertEquals(code.pop().toChar(), '-')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '*')
        assertEquals(code.pop().toChar(), '-')
        assertEquals(code.pop().toChar(), '-')
    }

    @Test
    fun testChannelCodeReaderFilter() {
        // create a windowing channel that drops the 2 first characters, keeps 6 characters and drops the rest of the line
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(ChannelCodeReaderFilter(Any(), WindowingChannel()))
        val code = CodeBuffer("0123456789\nABCDEFGHIJ", configuration)
        // test #charAt
        assertEquals('2', code[0])
        assertEquals('7', code[5])
        assertEquals('\n', code[6])
        assertEquals('C', code[7])
        assertEquals('H', code[12])
        assertEquals(-1, code.intAt(13))
        // test peek and pop
        assertEquals(code.peek().toChar(), '2')
        assertEquals(code.pop().toChar(), '2')
        assertEquals(code.pop().toChar(), '3')
        assertEquals(code.pop().toChar(), '4')
        assertEquals(code.pop().toChar(), '5')
        assertEquals(code.pop().toChar(), '6')
        assertEquals(code.pop().toChar(), '7') // and 8 shouldn't show up
        assertEquals(code.pop().toChar(), '\n')
        assertEquals(code.peek().toChar(), 'C')
        assertEquals(code.pop().toChar(), 'C')
        assertEquals(code.pop().toChar(), 'D')
        assertEquals(code.pop().toChar(), 'E')
        assertEquals(code.pop().toChar(), 'F')
        assertEquals(code.pop().toChar(), 'G')
        assertEquals(code.pop().toChar(), 'H')
        assertEquals(code.pop(), -1)
    }

    /**
     * Backward compatibility with a COBOL plugin: filter returns 0 instead of -1, when end of the stream has been reached.
     */
    @Test
    fun testWrongEndOfStreamFilter() {
        assertTimeout(ofSeconds(1)) {
            val configuration = CodeReaderConfiguration()
            configuration.setCodeReaderFilters(WrongEndOfStreamFilter())
            CodeBuffer("foo", configuration)
        }
    }

    internal inner class WrongEndOfStreamFilter : CodeReaderFilter<Any>() {
        override fun read(filteredBuffer: CharArray, offset: Int, length: Int): Int {
            return 0
        }
    }

    internal inner class ReplaceNumbersFilter : CodeReaderFilter<Any>() {
        private val pattern = Pattern.compile("\\d")
        private val REPLACEMENT = "-"
        override fun read(cbuf: CharArray, off: Int, len: Int): Int {
            val tempBuffer = CharArray(cbuf.size)
            val charCount = getReader().read(tempBuffer, off, len)
            if (charCount != -1) {
                val filteredString = pattern.matcher(String(tempBuffer)).replaceAll(REPLACEMENT)
                System.arraycopy(filteredString.toCharArray(), 0, cbuf, 0, tempBuffer.size)
            }
            return charCount
        }
    }

    internal inner class ReplaceCharFilter : CodeReaderFilter<Any>() {
        private val pattern = Pattern.compile("[a-zA-Z]")
        private val REPLACEMENT = "*"
        override fun read(cbuf: CharArray, off: Int, len: Int): Int {
            val tempBuffer = CharArray(cbuf.size)
            val charCount = getReader().read(tempBuffer, off, len)
            if (charCount != -1) {
                val filteredString = pattern.matcher(String(tempBuffer)).replaceAll(REPLACEMENT)
                System.arraycopy(filteredString.toCharArray(), 0, cbuf, 0, tempBuffer.size)
            }
            return charCount
        }
    }

    internal inner class WindowingChannel : Channel<Any>() {
        override fun consume(code: CodeReader, output: Any): Boolean {
            val columnPosition = code.getColumnPosition()
            if (code.peek() == '\n'.code) {
                return false
            }
            if (columnPosition < 2 || columnPosition > 7) {
                code.pop()
                return true
            }
            return false
        }
    }
}