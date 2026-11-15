//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import java.util.regex.Pattern

public class IdentifierAndKeywordChannel(regexp: String, private val caseSensitive: Boolean, vararg keywordSets: Array<out TokenType>) :
    Channel<LexerOutput> {
    private val keywordsMap = keywordSets.flatten().associateBy(
        { if (caseSensitive) it.value else it.value.uppercase() },
        { it })
    private val pattern = Pattern.compile(regexp)

    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        val tmpBuilder = StringBuilder()
        val matcher = pattern.matcher("")

        if (code.popTo(matcher, tmpBuilder) > 0) {
            var word = tmpBuilder.toString()
            val wordOriginal = word
            if (!caseSensitive) {
                word = word.uppercase()
            }
            val keywordType = keywordsMap[word]
            val token = Token.builder()
                .setType(keywordType ?: GenericTokenType.IDENTIFIER)
                .setValueAndOriginalValue(word, wordOriginal)
                .setLine(code.previousCursor.line)
                .setColumn(code.previousCursor.column)
                .build()
            output.addToken(token)
            return true
        }
        return false
    }
}
