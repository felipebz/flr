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

import java.io.IOException
import java.io.Reader
import java.util.regex.Matcher

/**
 * The CodeReader class provides some advanced features to read a source code. The most important one is the ability to try consuming the
 * next characters in the stream according to a regular expression.
 */
class CodeReader : CodeBuffer {
    private var _previousCursor: Cursor? = null

    val previousCursor: Cursor
        get() = _previousCursor.let {
            checkNotNull(it)
            return it
        }

    /*
   * Constructor needed to be backward compatible (before using CodeReaderFilter)
   */
    constructor(code: Reader) : super(code, CodeReaderConfiguration())

    /*
   * Constructor needed to be backward compatible (before using CodeReaderFilter)
   */
    constructor(code: String) : super(code, CodeReaderConfiguration())

    /**
     * Creates a code reader with specific configuration parameters.
     * Note that this constructor will read everything from reader and will close it.
     *
     * @param code
     * the Reader to read code from
     * @param configuration
     * the configuration parameters
     */
    constructor(code: Reader, configuration: CodeReaderConfiguration) : super(code, configuration)

    /**
     * Creates a code reader with specific configuration parameters.
     *
     * @param code
     * the code itself
     * @param configuration
     * the configuration parameters
     */
    constructor(code: String, configuration: CodeReaderConfiguration) : super(code, configuration)

    /**
     * Read and consume the next character
     *
     * @param appendable
     * the read character is appended to appendable
     */
    fun pop(appendable: Appendable) {
        try {
            appendable.append(pop().toChar())
        } catch (e: IOException) {
            throw ChannelException(e.message, e)
        }
    }

    /**
     * Read without consuming the next characters
     *
     * @param length
     * number of character to read
     * @return array of characters
     */
    fun peek(length: Int): CharArray {
        val result = CharArray(length)
        var index = 0
        var nextChar = intAt(index)
        while (nextChar != -1 && index < length) {
            result[index] = nextChar.toChar()
            index++
            nextChar = intAt(index)
        }
        return result
    }

    /**
     * Read without consuming the next characters until a condition is reached (EndMatcher)
     *
     * @param matcher
     * the EndMatcher used to stop the reading
     * @param appendable
     * the read characters is appended to appendable
     */
    fun peekTo(matcher: EndMatcher, appendable: Appendable) {
        var index = 0
        var nextChar = intAt(index)
        try {
            while (!matcher.match(nextChar) && nextChar != -1) {
                appendable.append(nextChar.toChar())
                ++index
                nextChar = intAt(index)
            }
        } catch (e: IOException) {
            throw ChannelException(e.message, e)
        }
    }

    /**
     * Read and consume the next characters according to a given regular expression
     *
     * @param matcher
     * the regular expression matcher
     * @param appendable
     * the consumed characters are appended to this appendable
     * @return number of consumed characters or -1 if the next input sequence doesn't match this matcher's pattern
     */
    fun popTo(matcher: Matcher, appendable: Appendable): Int {
        return popTo(matcher, null, appendable)
    }

    /**
     * Read and consume the next characters according to a given regular expression. Moreover the character sequence immediately following the
     * desired characters must also match a given regular expression.
     *
     * @param matcher
     * the Matcher used to try consuming next characters
     * @param afterMatcher
     * the Matcher used to check character sequence immediately following the consumed characters
     * @param appendable
     * the consumed characters are appended to this appendable
     * @return number of consumed characters or -1 if one of the two Matchers doesn't match
     */
    fun popTo(matcher: Matcher, afterMatcher: Matcher?, appendable: Appendable): Int {
        try {
            matcher.reset(this)
            if (matcher.lookingAt()) {
                if (afterMatcher != null) {
                    afterMatcher.reset(this)
                    afterMatcher.region(matcher.end(), length)
                    if (!afterMatcher.lookingAt()) {
                        return -1
                    }
                }
                _previousCursor = getCursor().clone()
                for (i in 0 until matcher.end()) {
                    appendable.append(pop().toChar())
                }
                return matcher.end()
            }
        } catch (e: StackOverflowError) {
            throw ChannelException(
                "Unable to apply regular expression '" + matcher.pattern().pattern()
                        + "' at line " + getCursor().line + " and column " + getCursor().column
                        + ", because it led to a stack overflow error."
                        + " This error may be due to an inefficient use of alternations - see https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507",
                e
            )
        } catch (e: IndexOutOfBoundsException) {
            return -1
        } catch (e: IOException) {
            throw ChannelException(e.message, e)
        } finally {
            // Avoid keeping a reference to the "CodeReader" instance:
            matcher.reset("")
        }
        return -1
    }
}