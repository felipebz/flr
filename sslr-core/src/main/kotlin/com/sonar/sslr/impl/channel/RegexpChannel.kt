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

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.Lexer
import com.sonar.sslr.impl.LexerException
import org.sonar.sslr.channel.Channel
import org.sonar.sslr.channel.CodeReader
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Creates token of specified type from characters, which match given regular expression.
 *
 * @see RegexpChannelBuilder
 */
class RegexpChannel(private val type: TokenType, private val regexp: String) : Channel<Lexer>() {
    private val tmpBuilder = StringBuilder()
    private val matcher: Matcher = Pattern.compile(regexp).matcher("")
    private val tokenBuilder: Token.Builder = Token.builder()
    override fun consume(code: CodeReader, output: Lexer): Boolean {
        return try {
            if (code.popTo(matcher, tmpBuilder) > 0) {
                val value = tmpBuilder.toString()
                val token = tokenBuilder
                    .setType(type)
                    .setValueAndOriginalValue(value)
                    .setURI(output.uri)
                    .setLine(code.previousCursor.line)
                    .setColumn(code.previousCursor.column)
                    .build()
                output.addToken(token)
                tmpBuilder.delete(0, tmpBuilder.length)
                return true
            }
            false
        } catch (e: StackOverflowError) {
            throw LexerException(
                "The regular expression "
                        + regexp
                        + " has led to a stack overflow error. "
                        + "This error is certainly due to an inefficient use of alternations. See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507",
                e
            )
        }
    }

}