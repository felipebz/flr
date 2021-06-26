/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.tests

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Grammar
import com.sonar.sslr.api.Rule
import com.sonar.sslr.impl.Lexer
import com.sonar.sslr.impl.Parser
import com.sonar.sslr.impl.channel.BlackHoleChannel
import com.sonar.sslr.impl.channel.RegexpChannel
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ParserAssertTest {
    private lateinit var rule: Rule
    private lateinit var parser: Parser<*>
    @Before
    fun setUp() {
        val lexer = Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(RegexpChannel(GenericTokenType.IDENTIFIER, "[a-z]++"))
            .withChannel(BlackHoleChannel(" "))
            .build()
        rule = RuleDefinition("ruleName").`is`("foo")
        val grammar: Grammar = object : Grammar() {
            override fun getRootRule(): Rule {
                return rule
            }
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
        val thrown = Assert.assertThrows(
            ParsingResultComparisonFailure::class.java
        ) { ParserAssert(parser).matches("bar") }
        Assert.assertTrue(thrown.message.contains("Rule 'ruleName' should match:\nbar"))
    }

    @Test
    fun test2_matches_failure() {
        val thrown = Assert.assertThrows(
            ParsingResultComparisonFailure::class.java
        ) { ParserAssert(parser).matches("foo bar") }
        Assert.assertTrue(thrown.message.contains("Rule 'ruleName' should match:\nfoo bar"))
    }

    @Test
    fun test_notMatches_failure() {
        val thrown = Assert.assertThrows(
            AssertionError::class.java
        ) { ParserAssert(parser).notMatches("foo") }
        Assert.assertEquals("Rule 'ruleName' should not match:\nfoo", thrown.message)
    }

    @Test
    fun test_notMatches_failure2() {
        rule.override("foo", GenericTokenType.EOF)
        val thrown = Assert.assertThrows(
            AssertionError::class.java
        ) { ParserAssert(parser).notMatches("foo") }
        Assert.assertEquals("Rule 'ruleName' should not match:\nfoo", thrown.message)
    }

    @Test
    fun should_not_accept_null_root_rule() {
        parser.setRootRule(null)
        val thrown = Assert.assertThrows(
            AssertionError::class.java
        ) { ParserAssert(parser).matches("") }
        Assert.assertEquals("Root rule of the parser should not be null", thrown.message)
    }

    @Test
    fun test_lexer_failure() {
        val thrown = Assert.assertThrows(
            ParsingResultComparisonFailure::class.java
        ) { ParserAssert(parser).matches("_") }
        val expectedMessage = StringBuilder()
            .append("Rule 'ruleName' should match:\n")
            .append("_\n")
            .append("Lexer error: Unable to lex")
            .toString()
        Assert.assertTrue(thrown.message.contains(expectedMessage))
    }
}