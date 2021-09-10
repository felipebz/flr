/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package org.sonar.sslr.channel

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The RegexChannel can be used to be called each time the next characters in the character stream match a regular expression
 */
public abstract class RegexChannel<O>(regex: String) : Channel<O> {
    private val tmpBuilder = StringBuilder()
    private val matcher: Matcher = Pattern.compile(regex).matcher("")
    override fun consume(code: CodeReader, output: O): Boolean {
        if (code.popTo(matcher, tmpBuilder) > 0) {
            consume(tmpBuilder, output)
            tmpBuilder.delete(0, tmpBuilder.length)
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