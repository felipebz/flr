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
package com.felipebz.flr.internal.matchers

public object TextUtils {
    public const val CR: Char = '\r'
    public const val LF: Char = '\n'
    private val ESCAPE = charArrayOf('\r', '\n', '\u000C', '\t', '"')
    private val ESCAPED = arrayOf("\\r", "\\n", "\\f", "\\t", "\\\"")

    /**
     * Replaces carriage returns, line feeds, form feeds, tabs and double quotes
     * with their respective escape sequences.
     */
    @JvmStatic
    public fun escape(ch: Char): String {
        for (i in ESCAPE.indices) {
            if (ESCAPE[i] == ch) {
                return ESCAPED[i]
            }
        }
        return ch.toString()
    }

    // TODO Godin: can be replaced by com.google.common.base.CharMatcher.anyOf("\n\r").trimTrailingFrom(string)
    @JvmStatic
    public fun trimTrailingLineSeparatorFrom(string: String): String {
        var last: Int = string.length - 1
        while (last >= 0) {
            if (string[last] != LF && string[last] != CR) {
                break
            }
            last--
        }
        return string.substring(0, last + 1)
    }
}