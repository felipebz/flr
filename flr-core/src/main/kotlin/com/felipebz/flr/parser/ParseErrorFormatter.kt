/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.parser

import com.felipebz.flr.internal.matchers.InputBuffer
import com.felipebz.flr.internal.matchers.TextUtils
import kotlin.math.max
import kotlin.math.min

/**
 * Formats [ParseError] to readable form.
 *
 *
 * This class is not intended to be subclassed by clients.
 *
 * @since 1.16
 */
public class ParseErrorFormatter {
    public fun format(parseError: ParseError?): String {
        requireNotNull(parseError)
        val inputBuffer = parseError.getInputBuffer()
        val position = inputBuffer.getPosition(parseError.getErrorIndex())
        val sb = StringBuilder()
        sb.append("Parse error at line ").append(position.getLine())
            .append(" column ").append(position.getColumn())
            .append(":\n\n")
        appendSnippet(sb, inputBuffer, position)
        return sb.toString()
    }

    public companion object {
        /**
         * Number of lines in snippet before and after line with error.
         */
        private const val SNIPPET_SIZE = 10
        private fun appendSnippet(sb: StringBuilder, inputBuffer: InputBuffer, position: InputBuffer.Position) {
            val startLine = max(position.getLine() - SNIPPET_SIZE, 1)
            val endLine = min(position.getLine() + SNIPPET_SIZE, inputBuffer.getLineCount())
            val padding = endLine.toString().length
            val lineNumberFormat = "%1$" + padding + "d: "
            for (line in startLine..endLine) {
                sb.append(String.format(lineNumberFormat, line))
                sb.append(TextUtils.trimTrailingLineSeparatorFrom(inputBuffer.extractLine(line)).replace("\t", " "))
                    .append('\n')
                if (line == position.getLine()) {
                    for (i in 1 until position.getColumn() + padding + 2) {
                        sb.append(' ')
                    }
                    sb.append("^\n")
                }
            }
        }
    }
}