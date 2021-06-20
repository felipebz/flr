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
package org.sonar.sslr.parser

import org.sonar.sslr.internal.matchers.InputBuffer
import org.sonar.sslr.internal.matchers.ParseNode

/**
 * Parsing result.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 *
 * @since 1.16
 */
class ParsingResult(
    private val inputBuffer: InputBuffer,
    private val matched: Boolean,
    private val parseTreeRoot: ParseNode?,
    private val parseError: ParseError?
) {

    fun getInputBuffer(): InputBuffer {
        return inputBuffer
    }

    fun isMatched(): Boolean {
        return matched
    }

    fun getParseError(): ParseError? {
        return parseError
    }

    // @VisibleForTesting
    fun getParseTreeRoot(): ParseNode {
        return checkNotNull(parseTreeRoot)
    }

}