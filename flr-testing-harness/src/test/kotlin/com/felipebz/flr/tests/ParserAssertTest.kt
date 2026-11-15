//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.RegexpChannel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserAssertTest {
    private lateinit var parser: Parser<*>
    @BeforeEach
    fun setUp() {
        parser = parser("foo")
    }

    /** A parser whose root rule `ruleName` is the sequence [first], [rest]. */
    private fun parser(first: Any, vararg rest: Any): Parser<*> {
        val lexer = Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(RegexpChannel(GenericTokenType.IDENTIFIER, "[a-z]++"))
            .withChannel(BlackHoleChannel(" "))
            .build()
        val key = object : GrammarRuleKey {
            override fun toString() = "ruleName"
        }
        val b = LexerfulGrammarBuilder.create()
        b.rule(key).`is`(first, *rest)
        b.setRootRule(key)
        return Parser.builder(b.build()).withLexer(lexer).build()
    }

    @Test
    fun ok() {
        ParserAssert(parser)
            .matches("foo")
            .notMatches("bar")
            .notMatches("foo foo")
    }

    @Test
    fun test_matches_failure() {
        val thrown = assertThrows<ParsingResultComparisonFailure> {
            ParserAssert(parser).matches("bar")
        }
        assertTrue(thrown.message.contains("Rule 'ruleName' should match:\nbar"))
    }

    @Test
    fun test2_matches_failure() {
        val thrown = assertThrows<ParsingResultComparisonFailure> {
            ParserAssert(parser).matches("foo bar")
        }
        assertTrue(thrown.message.contains("Rule 'ruleName' should match:\nfoo bar"))
    }

    @Test
    fun test_notMatches_failure() {
        val thrown = assertThrows<AssertionError> {
            ParserAssert(parser).notMatches("foo")
        }
        assertEquals("Rule 'ruleName' should not match:\nfoo", thrown.message)
    }

    @Test
    fun test_notMatches_failure2() {
        parser = parser("foo", GenericTokenType.EOF)
        val thrown = assertThrows<AssertionError> {
            ParserAssert(parser).notMatches("foo")
        }
        assertEquals("Rule 'ruleName' should not match:\nfoo", thrown.message)
    }

    @Test
    fun test_lexer_failure() {
        val thrown = assertThrows<ParsingResultComparisonFailure> {
            ParserAssert(parser).matches("_")
        }
        val expectedMessage = StringBuilder()
            .append("Rule 'ruleName' should match:\n")
            .append("_\n")
            .append("Lexer error: Unable to lex")
            .toString()
        assertTrue(thrown.message.contains(expectedMessage))
    }
}
