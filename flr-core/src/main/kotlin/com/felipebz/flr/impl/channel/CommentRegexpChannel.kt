//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.Trivia
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerException
import com.felipebz.flr.impl.LexerOutput
import java.util.regex.Pattern

/**
 * Creates comment trivia from characters, which match given regular expression.
 *
 * @see RegexpChannelBuilder
 */
public class CommentRegexpChannel(private val regexp: String) : Channel<LexerOutput> {
    private val pattern = Pattern.compile(regexp)

    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        val tmpBuilder = output.tokenText()
        val matcher = pattern.matcher("")

        return try {
            if (code.popTo(matcher, tmpBuilder) > 0) {
                val value = output.sharedString(tmpBuilder)
                val token = Token.builder()
                    .setType(GenericTokenType.COMMENT)
                    .setValueAndOriginalValue(value)
                    .setLine(code.previousCursor.line)
                    .setColumn(code.previousCursor.column)
                    .build()
                output.addTrivia(Trivia.createComment(token))
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
