/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
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

    /**
     * @since 1.16
     */
    @JvmOverloads
    public constructor(line: Int, column: Int, message: String, cause: Throwable? = null) : super(message, cause) {
        this.line = line
        this.column = column
    }

}
