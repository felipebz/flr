//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.channel

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The RegexChannel can be used to be called each time the next characters in the character stream match a regular expression
 */
public abstract class RegexChannel<O>(regex: String) : Channel<O> {
    private val matcher: Matcher = Pattern.compile(regex).matcher("")
    override fun consume(code: CodeReader, output: O): Boolean {
        val tmpBuilder = StringBuilder()
        if (code.popTo(matcher, tmpBuilder) > 0) {
            consume(tmpBuilder, output)
            return true
        }
        return false
    }

    /**
     * The consume method is called each time the regular expression used to create the RegexChannel object matches the next characters in the
     * character streams.
     *
     * @param token
     * the token consumed in the character stream and matching the regular expression
     * @param output
     * the OUTPUT object which can be optionally fed
     */
    protected abstract fun consume(token: CharSequence?, output: O)

}
