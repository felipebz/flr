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

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.test.lexer.LexerMatchers.hasOriginalToken
import com.sonar.sslr.test.lexer.LexerMatchers.hasToken
import com.sonar.sslr.test.lexer.MockHelper.mockLexer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.sonar.sslr.channel.CodeReader
import org.sonar.sslr.test.channel.ChannelMatchers.consume

class IdentifierAndKeywordChannelTest {
    private var channel: IdentifierAndKeywordChannel? = null
    private val lexer = mockLexer()
    @Test
    fun testConsumeWord() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.values())
        assertThat(channel, consume("word", lexer))
        assertThat(lexer.tokens, hasToken("word", GenericTokenType.IDENTIFIER))
    }

    @Test
    fun testConsumeCaseSensitiveKeywords() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.values())
        assertThat(channel, consume("KEYWORD1", lexer))
        assertThat(lexer.tokens, hasToken("KEYWORD1", MyKeywords.KEYWORD1))
        assertThat(channel, consume("KeyWord2", lexer))
        assertThat(lexer.tokens, hasToken("KeyWord2", MyKeywords.KeyWord2))
        assertThat(channel, consume("KEYWORD2", lexer))
        assertThat(lexer.tokens, hasToken("KEYWORD2", GenericTokenType.IDENTIFIER))
    }

    @Test
    fun testConsumeNotCaseSensitiveKeywords() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.values())
        assertThat(channel, consume("keyword1", lexer))
        assertThat(lexer.tokens, hasToken("KEYWORD1", MyKeywords.KEYWORD1))
        assertThat(lexer.tokens, hasToken("KEYWORD1"))
        assertThat(lexer.tokens, hasOriginalToken("keyword1"))
        assertThat(channel, consume("keyword2", lexer))
        assertThat(lexer.tokens, hasToken("KEYWORD2", MyKeywords.KeyWord2))
    }

    @Test
    fun testColumnAndLineNumbers() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.values())
        val reader = CodeReader("\n\n  keyword1")
        reader.pop()
        reader.pop()
        reader.pop()
        reader.pop()
        assertThat(channel, consume(reader, lexer))
        val keyword = lexer.tokens[0]
        assertEquals(keyword.column, 2)
        assertEquals(keyword.line, 3)
    }

    @Test
    fun testNotConsumNumber() {
        assertThat(channel, Matchers.not(consume("1234", lexer)))
    }

    private enum class MyKeywords : TokenType {
        KEYWORD1, KeyWord2;

        override val value: String
            get() = name

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }
}