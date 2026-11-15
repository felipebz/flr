//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.internal.matchers.InputBuffer
import com.felipebz.flr.internal.matchers.TextUtils
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Formats [ParseError] to readable form.
 *
 *
 * This class is not intended to be subclassed by clients.
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
                sb.append(String.format(Locale.ROOT, lineNumberFormat, line))
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
