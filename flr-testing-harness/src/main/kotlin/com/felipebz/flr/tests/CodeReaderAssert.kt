//
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
