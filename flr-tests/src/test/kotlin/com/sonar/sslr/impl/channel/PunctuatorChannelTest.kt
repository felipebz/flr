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
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.test.lexer.LexerMatchers.hasToken
import com.sonar.sslr.test.lexer.MockHelper.mockLexer
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.sonar.sslr.channel.CodeReader
import org.sonar.sslr.test.channel.ChannelMatchers.consume

class PunctuatorChannelTest {
    private val channel = PunctuatorChannel(*MyPunctuatorAndOperator.values())
    private val lexer = mockLexer()
    @Test
    fun testConsumeSpecialCharacters() {
        assertThat(channel, consume("**=", lexer))
        assertThat(lexer.tokens, hasToken("*", MyPunctuatorAndOperator.STAR))
        assertThat(channel, consume(",=", lexer))
        assertThat(lexer.tokens, hasToken(",", MyPunctuatorAndOperator.COLON))
        assertThat(channel, consume("=*", lexer))
        assertThat(lexer.tokens, hasToken("=", MyPunctuatorAndOperator.EQUAL))
        assertThat(channel, consume("==,", lexer))
        assertThat(lexer.tokens, hasToken("==", MyPunctuatorAndOperator.EQUAL_OP))
        assertThat(channel, consume("*=,", lexer))
        assertThat(lexer.tokens, hasToken("*=", MyPunctuatorAndOperator.MUL_ASSIGN))
        assertFalse(channel.consume(CodeReader("!"), lexer))
    }

    @Test
    fun testNotConsumeWord() {
        assertFalse(channel.consume(CodeReader("word"), lexer))
    }

    private enum class MyPunctuatorAndOperator(override val value: String) : TokenType {
        STAR("*"), COLON(","), EQUAL("="), EQUAL_OP("=="), MUL_ASSIGN("*="), NOT_EQUAL("!=");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }
}