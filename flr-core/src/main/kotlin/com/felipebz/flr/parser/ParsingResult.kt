//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.internal.matchers.InputBuffer
import com.felipebz.flr.internal.matchers.ParseNode

/**
 * Parsing result.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class ParsingResult(
    private val inputBuffer: InputBuffer,
    private val matched: Boolean,
    private val parseTreeRoot: ParseNode?,
    private val parseError: ParseError?
) {

    public fun getInputBuffer(): InputBuffer {
        return inputBuffer
    }

    public fun isMatched(): Boolean {
        return matched
    }

    public fun getParseError(): ParseError? {
        return parseError
    }

    // @VisibleForTesting
    public fun getParseTreeRoot(): ParseNode {
        return checkNotNull(parseTreeRoot)
    }

}
