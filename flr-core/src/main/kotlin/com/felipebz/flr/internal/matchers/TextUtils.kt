//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
