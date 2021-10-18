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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.Trivia
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerException
import com.felipebz.flr.impl.LexerOutput
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Creates comment trivia from characters, which match given regular expression.
 *
 * @see RegexpChannelBuilder
 */
public class CommentRegexpChannel(private val regexp: String) : Channel<LexerOutput> {
    private val tmpBuilder = StringBuilder()
    private val matcher: Matcher = Pattern.compile(regexp).matcher("")
    private val tokenBuilder: Token.Builder = Token.builder()
    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        return try {
            if (code.popTo(matcher, tmpBuilder) > 0) {
                val value = tmpBuilder.toString()
                val token = tokenBuilder
                    .setType(GenericTokenType.COMMENT)
                    .setValueAndOriginalValue(value)
                    .setLine(code.previousCursor.line)
                    .setColumn(code.previousCursor.column)
                    .build()
                output.addTrivia(Trivia.createComment(token))
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