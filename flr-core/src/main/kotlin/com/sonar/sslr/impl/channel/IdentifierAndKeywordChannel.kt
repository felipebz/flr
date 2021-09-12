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
import org.sonar.sslr.channel.Channel
import org.sonar.sslr.channel.CodeReader
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.HashMap

public class IdentifierAndKeywordChannel(regexp: String, caseSensitive: Boolean, vararg keywordSets: Array<out TokenType>) :
    Channel<Lexer> {
    private val keywordsMap: MutableMap<String?, TokenType> = HashMap()
    private val tmpBuilder = StringBuilder()
    private val matcher: Matcher
    private val caseSensitive: Boolean
    private val tokenBuilder: Token.Builder = Token.builder()

    /**
     * @throws PatternSyntaxException if the expression's syntax is invalid
     */
    init {
        for (keywords in keywordSets) {
            for (keyword in keywords) {
                val keywordValue = if (caseSensitive) keyword.value else keyword.value.uppercase(Locale.getDefault())
                keywordsMap[keywordValue] = keyword
            }
        }
        this.caseSensitive = caseSensitive
        matcher = Pattern.compile(regexp).matcher("")
    }

    override fun consume(code: CodeReader, output: Lexer): Boolean {
        if (code.popTo(matcher, tmpBuilder) > 0) {
            var word = tmpBuilder.toString()
            val wordOriginal = word
            if (!caseSensitive) {
                word = word.uppercase(Locale.getDefault())
            }
            val keywordType = keywordsMap[word]
            val token = tokenBuilder
                .setType(keywordType ?: GenericTokenType.IDENTIFIER)
                .setValueAndOriginalValue(word, wordOriginal)
                .setURI(output.uri)
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