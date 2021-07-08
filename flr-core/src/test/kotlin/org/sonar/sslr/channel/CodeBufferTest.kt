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

import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.util.regex.Pattern

class CodeBufferTest {
    private val defaulConfiguration = CodeReaderConfiguration()
    @Test
    fun testPop() {
        val code = CodeBuffer("pa", defaulConfiguration)
        Assert.assertThat(code.pop().toChar(), Is.`is`('p'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('a'))
        Assert.assertThat(code.pop(), Is.`is`(-1))
    }

    @Test
    fun testPeek() {
        val code = CodeBuffer("pa", defaulConfiguration)
        Assert.assertThat(code.peek().toChar(), Is.`is`('p'))
        Assert.assertThat(code.peek().toChar(), Is.`is`('p'))
        code.pop()
        Assert.assertThat(code.peek().toChar(), Is.`is`('a'))
        code.pop()
        Assert.assertThat(code.peek(), Is.`is`(-1))
    }

    @Test
    fun testLastCharacter() {
        val reader = CodeBuffer("bar", defaulConfiguration)
        Assert.assertThat(reader.lastChar(), Is.`is`(-1))
        reader.pop()
        Assert.assertThat(reader.lastChar().toChar(), Is.`is`('b'))
    }

    @Test
    fun testGetColumnAndLinePosition() {
        val reader = CodeBuffer("pa\nc\r\ns\r\n\r\n", defaulConfiguration)
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(1))
        reader.pop() // p
        reader.pop() // a
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(2))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(1))
        reader.peek() // \n
        reader.lastChar() // a
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(2))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(1))
        reader.pop() // \n
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(2))
        reader.pop() // c
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(1))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(2))
        reader.pop() // \r
        reader.pop() // \n
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(3))
        Assert.assertThat(reader.pop().toChar(), Is.`is`('s'))
        reader.pop() // \r
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(2))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(3))
        reader.pop() // \n
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(4))
        reader.pop() // \r
        reader.pop() // \n
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(5))
    }

    @Test
    fun testStartAndStopRecording() {
        val reader = CodeBuffer("123456", defaulConfiguration)
        reader.pop()
        Assert.assertEquals("", reader.stopRecording().toString())
        reader.startRecording()
        reader.pop()
        reader.pop()
        reader.peek()
        Assert.assertEquals("23", reader.stopRecording().toString())
        Assert.assertEquals("", reader.stopRecording().toString())
    }

    @Test
    fun testCharAt() {
        val reader = CodeBuffer("123456", defaulConfiguration)
        Assert.assertEquals('1', reader[0])
        Assert.assertEquals('6', reader[5])
    }

    @Test
    fun testCharAtIndexOutOfBoundsException() {
        val reader = CodeBuffer("12345", defaulConfiguration)
        Assert.assertEquals(reader[5], (-1).toChar())
    }

    @Test
    fun testReadWithSpecificTabWidth() {
        val configuration = CodeReaderConfiguration()
        configuration.setTabWidth(4)
        val reader = CodeBuffer("pa\n\tc", configuration)
        Assert.assertEquals('\n', reader[2])
        Assert.assertEquals('\t', reader[3])
        Assert.assertEquals('c', reader[4])
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(1))
        reader.pop() // p
        reader.pop() // a
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(2))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(1))
        reader.peek() // \n
        reader.lastChar() // a
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(2))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(1))
        reader.pop() // \n
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(0))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(2))
        reader.pop() // \t
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(4))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(2))
        reader.pop() // c
        Assert.assertThat(reader.getColumnPosition(), Is.`is`(5))
        Assert.assertThat(reader.getLinePosition(), Is.`is`(2))
    }

    @Test
    @Throws(Exception::class)
    fun testCodeReaderFilter() {
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(ReplaceNumbersFilter())
        val code = CodeBuffer("abcd12efgh34", configuration)
        // test #charAt
        Assert.assertEquals('a', code[0])
        Assert.assertEquals('-', code[4])
        Assert.assertEquals('-', code[5])
        Assert.assertEquals('e', code[6])
        Assert.assertEquals('-', code[10])
        Assert.assertEquals('-', code[11])
        // test peek and pop
        Assert.assertThat(code.peek().toChar(), Is.`is`('a'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('a'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('b'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('c'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('d'))
        Assert.assertThat(code.peek().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('e'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('f'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('g'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('h'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
    }

    @Test
    fun theLengthShouldBeTheSameThanTheStringLength() {
        val myCode = "myCode"
        Assert.assertThat(CodeBuffer(myCode, CodeReaderConfiguration()).length, Is.`is`(6))
    }

    @Test
    fun theLengthShouldDecreaseEachTimeTheInputStreamIsConsumed() {
        val myCode = "myCode"
        val codeBuffer = CodeBuffer(myCode, CodeReaderConfiguration())
        codeBuffer.pop()
        codeBuffer.pop()
        Assert.assertThat(codeBuffer.length, Is.`is`(4))
    }

    @Test
    @Throws(Exception::class)
    fun testSeveralCodeReaderFilter() {
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(ReplaceNumbersFilter(), ReplaceCharFilter())
        val code = CodeBuffer("abcd12efgh34", configuration)
        // test #charAt
        Assert.assertEquals('*', code[0])
        Assert.assertEquals('-', code[4])
        Assert.assertEquals('-', code[5])
        Assert.assertEquals('*', code[6])
        Assert.assertEquals('-', code[10])
        Assert.assertEquals('-', code[11])
        // test peek and pop
        Assert.assertThat(code.peek().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.peek().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('*'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('-'))
    }

    @Test
    @Throws(Exception::class)
    fun testChannelCodeReaderFilter() {
        // create a windowing channel that drops the 2 first characters, keeps 6 characters and drops the rest of the line
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(ChannelCodeReaderFilter(Any(), WindowingChannel()))
        val code = CodeBuffer("0123456789\nABCDEFGHIJ", configuration)
        // test #charAt
        Assert.assertEquals('2', code[0])
        Assert.assertEquals('7', code[5])
        Assert.assertEquals('\n', code[6])
        Assert.assertEquals('C', code[7])
        Assert.assertEquals('H', code[12])
        Assert.assertEquals(-1, code.intAt(13))
        // test peek and pop
        Assert.assertThat(code.peek().toChar(), Is.`is`('2'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('2'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('3'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('4'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('5'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('6'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('7')) // and 8 shouldn't show up
        Assert.assertThat(code.pop().toChar(), Is.`is`('\n'))
        Assert.assertThat(code.peek().toChar(), Is.`is`('C'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('C'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('D'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('E'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('F'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('G'))
        Assert.assertThat(code.pop().toChar(), Is.`is`('H'))
        Assert.assertThat(code.pop(), Is.`is`(-1))
    }

    /**
     * Backward compatibility with a COBOL plugin: filter returns 0 instead of -1, when end of the stream has been reached.
     */
    @Test(timeout = 1000)
    fun testWrongEndOfStreamFilter() {
        val configuration = CodeReaderConfiguration()
        configuration.setCodeReaderFilters(WrongEndOfStreamFilter())
        CodeBuffer("foo", configuration)
    }

    internal inner class WrongEndOfStreamFilter : CodeReaderFilter<Any>() {
        @Throws(IOException::class)
        override fun read(filteredBuffer: CharArray, offset: Int, length: Int): Int {
            return 0
        }
    }

    internal inner class ReplaceNumbersFilter : CodeReaderFilter<Any>() {
        private val pattern = Pattern.compile("\\d")
        private val REPLACEMENT = "-"
        @Throws(IOException::class)
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
        @Throws(IOException::class)
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