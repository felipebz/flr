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
package com.felipebz.flr.internal.matchers

/**
 * Input text to be parsed.
 *
 *
 * This interface is not intended to be implemented by clients.
 *
 * @since 1.16
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
