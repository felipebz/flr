//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.matchers

/**
 * Input text to be parsed.
 *
 *
 * This interface is not intended to be implemented by clients.
 */
public interface InputBuffer {
    public fun length(): Int
    public fun charAt(index: Int): Char

    /**
     * Returns content of a line for a given line number.
     * Numbering of lines starts from 1.
     */
    public fun extractLine(lineNumber: Int): String

    /**
     * Returns number of lines, which is always equal to number of line terminators plus 1.
     */
    public fun getLineCount(): Int
    public fun getPosition(index: Int): Position
    public class Position(private val line: Int, private val column: Int) {
        public fun getLine(): Int {
            return line
        }

        public fun getColumn(): Int {
            return column
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is Position) {
                return false
            }
            return (line == other.line
                    && column == other.column)
        }

        override fun hashCode(): Int {
            return 31 * line + column
        }

        override fun toString(): String {
            return "($line, $column)"
        }
    }
}
