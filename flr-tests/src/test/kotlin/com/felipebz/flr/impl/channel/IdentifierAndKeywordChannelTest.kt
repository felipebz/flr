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

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.test.channel.ChannelMatchers.consume
import com.felipebz.flr.test.lexer.LexerMatchers.hasOriginalToken
import com.felipebz.flr.test.lexer.LexerMatchers.hasToken
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdentifierAndKeywordChannelTest {
    private var channel: IdentifierAndKeywordChannel? = null
    private val output = LexerOutput()
    @Test
    fun testConsumeWord() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.entries.toTypedArray())
        assertThat(channel, consume("word", output))
        assertThat(output.tokens, hasToken("word", GenericTokenType.IDENTIFIER))
    }

    @Test
    fun testConsumeCaseSensitiveKeywords() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.entries.toTypedArray())
        assertThat(channel, consume("KEYWORD1", output))
        assertThat(output.tokens, hasToken("KEYWORD1", MyKeywords.KEYWORD1))
        assertThat(channel, consume("KeyWord2", output))
        assertThat(output.tokens, hasToken("KeyWord2", MyKeywords.KeyWord2))
        assertThat(channel, consume("KEYWORD2", output))
        assertThat(output.tokens, hasToken("KEYWORD2", GenericTokenType.IDENTIFIER))
    }

    @Test
    fun testConsumeNotCaseSensitiveKeywords() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.entries.toTypedArray())
        assertThat(channel, consume("keyword1", output))
        assertThat(output.tokens, hasToken("KEYWORD1", MyKeywords.KEYWORD1))
        assertThat(output.tokens, hasToken("KEYWORD1"))
        assertThat(output.tokens, hasOriginalToken("keyword1"))
        assertThat(channel, consume("keyword2", output))
        assertThat(output.tokens, hasToken("KEYWORD2", MyKeywords.KeyWord2))
    }

    @Test
    fun testColumnAndLineNumbers() {
        channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.entries.toTypedArray())
        val reader = CodeReader("\n\n  keyword1")
        reader.pop()
        reader.pop()
        reader.pop()
        reader.pop()
        assertThat(channel, consume(reader, output))
        val keyword = output.tokens[0]
        assertEquals(keyword.column, 2)
        assertEquals(keyword.line, 3)
    }

    @Test
    fun testNotConsumNumber() {
        assertThat(channel, Matchers.not(consume("1234", output)))
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