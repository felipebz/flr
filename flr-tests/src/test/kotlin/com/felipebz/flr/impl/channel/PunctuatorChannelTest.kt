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

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.test.channel.ChannelMatchers.consume
import com.felipebz.flr.test.lexer.LexerMatchers.hasToken
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class PunctuatorChannelTest {
    private val channel = PunctuatorChannel(*MyPunctuatorAndOperator.values())
    private val output = LexerOutput()
    @Test
    fun testConsumeSpecialCharacters() {
        assertThat(channel, consume("**=", output))
        assertThat(output.tokens, hasToken("*", MyPunctuatorAndOperator.STAR))
        assertThat(channel, consume(",=", output))
        assertThat(output.tokens, hasToken(",", MyPunctuatorAndOperator.COLON))
        assertThat(channel, consume("=*", output))
        assertThat(output.tokens, hasToken("=", MyPunctuatorAndOperator.EQUAL))
        assertThat(channel, consume("==,", output))
        assertThat(output.tokens, hasToken("==", MyPunctuatorAndOperator.EQUAL_OP))
        assertThat(channel, consume("*=,", output))
        assertThat(output.tokens, hasToken("*=", MyPunctuatorAndOperator.MUL_ASSIGN))
        assertFalse(channel.consume(CodeReader("!"), output))
    }

    @Test
    fun testNotConsumeWord() {
        assertFalse(channel.consume(CodeReader("word"), output))
    }

    private enum class MyPunctuatorAndOperator(override val value: String) : TokenType {
        STAR("*"), COLON(","), EQUAL("="), EQUAL_OP("=="), MUL_ASSIGN("*="), NOT_EQUAL("!=");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }
}