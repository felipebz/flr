//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class PunctuatorChannelTest {
    private val channel = PunctuatorChannel(*MyPunctuatorAndOperator.entries.toTypedArray())
    private val output = LexerOutput()
    @Test
    fun testConsumeSpecialCharacters() {
        assertThat(channel).consume("**=", output)
        assertThat(output.tokens).hasToken("*", MyPunctuatorAndOperator.STAR)
        assertThat(channel).consume(",=", output)
        assertThat(output.tokens).hasToken(",", MyPunctuatorAndOperator.COLON)
        assertThat(channel).consume("=*", output)
        assertThat(output.tokens).hasToken("=", MyPunctuatorAndOperator.EQUAL)
        assertThat(channel).consume("==,", output)
        assertThat(output.tokens).hasToken("==", MyPunctuatorAndOperator.EQUAL_OP)
        assertThat(channel).consume("*=,", output)
        assertThat(output.tokens).hasToken("*=", MyPunctuatorAndOperator.MUL_ASSIGN)
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
