/**
 * FLR
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.tests

import com.felipebz.flr.channel.CodeReader
import org.assertj.core.api.AbstractAssert

public class CodeReaderAssert(actual: CodeReader?) : AbstractAssert<CodeReaderAssert, CodeReader>(
    actual, CodeReaderAssert::class.java
) {

    public fun hasNextChar(nextChar: Char): CodeReaderAssert {
        isNotNull
        if (actual.peek() != nextChar.code) {
            failWithMessage("Expected next char to be '$nextChar' but was '${actual.peek().toChar()}'")
        }
        return this
    }

    public fun doesNotHaveNextChar(nextChar: Char): CodeReaderAssert {
        isNotNull
        if (actual.peek() == nextChar.code) {
            failWithMessage("Expected next char not to be '$nextChar' but was")
        }
        return this
    }

}
