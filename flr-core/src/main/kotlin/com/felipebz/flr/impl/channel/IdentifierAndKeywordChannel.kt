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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import java.util.*
import java.util.regex.Pattern

public class IdentifierAndKeywordChannel(regexp: String, private val caseSensitive: Boolean, vararg keywordSets: Array<out TokenType>) :
    Channel<LexerOutput> {
    private val keywordsMap = keywordSets.flatten().associateBy(
        { if (caseSensitive) it.value else it.value.uppercase(Locale.getDefault()) },
        { it })
    private val pattern = Pattern.compile(regexp)

    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        val tmpBuilder = StringBuilder()
        val matcher = pattern.matcher("")

        if (code.popTo(matcher, tmpBuilder) > 0) {
            var word = tmpBuilder.toString()
            val wordOriginal = word
            if (!caseSensitive) {
                word = word.uppercase(Locale.getDefault())
            }
            val keywordType = keywordsMap[word]
            val token = Token.builder()
                .setType(keywordType ?: GenericTokenType.IDENTIFIER)
                .setValueAndOriginalValue(word, wordOriginal)
                .setLine(code.previousCursor.line)
                .setColumn(code.previousCursor.column)
                .build()
            output.addToken(token)
            tmpBuilder.delete(0, tmpBuilder.length)
            return true
        }
        return false
    }
}