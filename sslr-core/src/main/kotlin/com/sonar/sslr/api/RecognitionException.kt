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
package com.sonar.sslr.api

import com.sonar.sslr.impl.LexerException

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
class RecognitionException : RuntimeException {

    /**
     * Line where the parsing error has occurred.
     *
     * @return line
     */
    val line: Int

    constructor(e: LexerException) : super("Lexer error: " + e.message, e) {
        line = 0
    }

    /**
     * @since 1.16
     */
    constructor(line: Int, message: String?) : super(message) {
        this.line = line
    }

    /**
     * @since 1.16
     */
    constructor(line: Int, message: String?, cause: Throwable?) : super(message, cause) {
        this.line = line
    }

}