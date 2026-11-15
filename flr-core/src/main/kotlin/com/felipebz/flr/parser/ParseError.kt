//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.internal.matchers.InputBuffer

/**
 * Describes an error, which is occurred during parse.
 * Use [ParseErrorFormatter] to convert instances of this class to readable format.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class ParseError(private val inputBuffer: InputBuffer, private val errorIndex: Int) {

    public fun getInputBuffer(): InputBuffer {
        return inputBuffer
    }

    public fun getErrorIndex(): Int {
        return errorIndex
    }

}
