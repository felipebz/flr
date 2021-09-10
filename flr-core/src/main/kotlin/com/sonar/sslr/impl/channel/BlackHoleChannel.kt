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
package com.sonar.sslr.impl.channel

import com.sonar.sslr.impl.Lexer
import org.sonar.sslr.channel.Channel
import org.sonar.sslr.channel.CodeReader
import java.io.IOException
import java.util.regex.Matcher
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
public class BlackHoleChannel(regexp: String) : Channel<Lexer> {
    private val matcher: Matcher = Pattern.compile(regexp).matcher("")
    override fun consume(code: CodeReader, output: Lexer): Boolean {
        return code.popTo(matcher, EmptyAppendable.INSTANCE) != -1
    }

    private class EmptyAppendable : Appendable {
        @Throws(IOException::class)
        override fun append(csq: CharSequence): Appendable {
            return this
        }

        @Throws(IOException::class)
        override fun append(c: Char): Appendable {
            return this
        }

        @Throws(IOException::class)
        override fun append(csq: CharSequence, start: Int, end: Int): Appendable {
            return this
        }

        companion object {
            val INSTANCE: Appendable = EmptyAppendable()
        }
    }

}