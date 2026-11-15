//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class IdentifierAndKeywordChannelTest {
    private val channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", true, MyKeywords.entries.toTypedArray())
    private val output = LexerOutput()
    @Test
    fun testConsumeWord() {
        assertThat(channel).consume("word", output)
        assertThat(output.tokens).hasToken("word", GenericTokenType.IDENTIFIER)
    }

    @Test
    fun testConsumeCaseSensitiveKeywords() {
        assertThat(channel).consume("KEYWORD1", output)
        assertThat(output.tokens).hasToken("KEYWORD1", MyKeywords.KEYWORD1)
        assertThat(channel).consume("KeyWord2", output)
        assertThat(output.tokens).hasToken("KeyWord2", MyKeywords.KeyWord2)
        assertThat(channel).consume("KEYWORD2", output)
        assertThat(output.tokens).hasToken("KEYWORD2", GenericTokenType.IDENTIFIER)
    }

    @Test
    fun testConsumeNotCaseSensitiveKeywords() {
        val channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.entries.toTypedArray())
        assertThat(channel).consume("keyword1", output)
        assertThat(output.tokens).hasToken("KEYWORD1", MyKeywords.KEYWORD1)
        assertThat(output.tokens).hasToken("KEYWORD1")
        assertThat(output.tokens).hasOriginalToken("keyword1")
        assertThat(channel).consume("keyword2", output)
        assertThat(output.tokens).hasToken("KEYWORD2", MyKeywords.KeyWord2)
    }

    @Test
    fun testConsumeNotCaseSensitiveWordsUnderTurkishLocale() {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("tr-TR"))
        try {
            val channel = IdentifierAndKeywordChannel("[a-zA-Z_][a-zA-Z_0-9]*", false, MyKeywords.entries.toTypedArray())
            for (word in listOf("INFO", "info", "Info")) {
                val output = LexerOutput()
                assertThat(channel).consume(word, output)
                assertThat(output.tokens).hasToken("INFO", MyKeywords.INFO)
            }
            for (word in listOf("ITEM", "item")) {
                val output = LexerOutput()
                assertThat(channel).consume(word, output)
                assertThat(output.tokens).hasToken("ITEM", GenericTokenType.IDENTIFIER)
            }
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun testColumnAndLineNumbers() {
        val reader = CodeReader("\n\n  keyword1")
        reader.pop()
        reader.pop()
        reader.pop()
        reader.pop()
        assertThat(channel).consume(reader, output)
        val keyword = output.tokens[0]
        assertEquals(keyword.column, 2)
        assertEquals(keyword.line, 3)
    }

    @Test
    fun testNotConsumNumber() {
        assertThat(channel).doesNotConsume("1234", output)
    }

    private enum class MyKeywords : TokenType {
        KEYWORD1, KeyWord2, INFO;

        override val value: String
            get() = name

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }
}
