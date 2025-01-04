/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.tests

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Grammar
import com.felipebz.flr.api.Rule
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.RegexpChannel
import com.felipebz.flr.impl.matcher.RuleDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserAssertTest {
    private lateinit var rule: Rule
    private lateinit var parser: Parser<*>
    @BeforeEach
    fun setUp() {
        val lexer = Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(RegexpChannel(GenericTokenType.IDENTIFIER, "[a-z]++"))
            .withChannel(BlackHoleChannel(" "))
            .build()
        rule = RuleDefinition("ruleName").`is`("foo")
        val grammar: Grammar = object : Grammar() {
            override val rootRule = rule
        }
        parser = Parser.builder(grammar).withLexer(lexer).build()
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
        rule.override("foo", GenericTokenType.EOF)
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
