/**
 * FLR
 * Copyright (C) 2021-2026 Felipe Zorzo
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
package com.felipebz.flr.impl

/**
 * The characters of the token being read, obtained from [LexerOutput.tokenText] and given to
 * [LexerOutput.sharedString]. It keeps the hash of its characters as they are appended, so that an equal text already
 * met in the same lex is found without creating a string.
 */
public class TokenTextBuilder internal constructor() : Appendable, CharSequence {
    private var chars = CharArray(INITIAL_CAPACITY)

    override var length: Int = 0
        private set

    /** The same value as [String.hashCode] of the characters appended so far. */
    internal var hash: Int = 0
        private set

    internal fun clear() {
        length = 0
        hash = 0
    }

    override fun append(c: Char): TokenTextBuilder {
        if (length == chars.size) {
            chars = chars.copyOf(length * 2)
        }
        chars[length++] = c
        hash = 31 * hash + c.code
        return this
    }

    override fun append(csq: CharSequence?): TokenTextBuilder {
        val text = csq ?: "null"
        return append(text, 0, text.length)
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): TokenTextBuilder {
        val text = csq ?: "null"
        for (i in start until end) {
            append(text[i])
        }
        return this
    }

    override fun get(index: Int): Char {
        if (index < 0 || index >= length) {
            throw IndexOutOfBoundsException("Index: $index, Length: $length")
        }
        return chars[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        if (startIndex < 0 || endIndex > length || startIndex > endIndex) {
            throw IndexOutOfBoundsException("Start: $startIndex, End: $endIndex, Length: $length")
        }
        return String(chars, startIndex, endIndex - startIndex)
    }

    /** Whether [string] has exactly the characters of this builder. */
    internal fun contentEquals(string: String): Boolean {
        if (string.length != length) {
            return false
        }
        for (i in 0 until length) {
            if (string[i] != chars[i]) {
                return false
            }
        }
        return true
    }

    override fun toString(): String = String(chars, 0, length)

    private companion object {
        const val INITIAL_CAPACITY = 64
    }
}
