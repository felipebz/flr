//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

import com.felipebz.flr.impl.LexerException

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class RecognitionException : RuntimeException {

    /**
     * Line where the parsing error has occurred.
     *
     * @return line
     */
    public val line: Int

    /**
     * Column where the parsing error has occurred.
     *
     * @return column
     */
    public val column: Int

    public constructor(e: LexerException) : super("Lexer error: " + e.message, e) {
        line = 0
        column = 0
    }

    @JvmOverloads
    public constructor(line: Int, column: Int, message: String, cause: Throwable? = null) : super(message, cause) {
        this.line = line
        this.column = column
    }

}
