//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import java.util.regex.Pattern

/**
 * Allows to skip characters, which match given regular expression.
 *
 *
 * Mostly this channel is used with regular expression "\s++" to remove all whitespace characters.
 * And in such case this channel should be the first one in a sequence of channels for performance reasons,
 * because generally whitespace characters are encountered more often than all other and especially between others.
 *
 */
public class BlackHoleChannel(regexp: String) : Channel<LexerOutput> {
    private val pattern = Pattern.compile(regexp)

    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        val matcher = pattern.matcher("")
        return code.popTo(matcher, EmptyAppendable) != -1
    }

    private object EmptyAppendable : Appendable {
        override fun append(csq: CharSequence?): Appendable {
            return this
        }

        override fun append(c: Char): Appendable {
            return this
        }

        override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
            return this
        }
    }

}
