//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RuleAssertTest {
    private lateinit var rule: Rule
    @BeforeEach
    fun setUp() {
        val key = object : GrammarRuleKey {
            override fun toString() = "ruleName"
        }
        val b = LexerlessGrammarBuilder.create()
        b.rule(key).`is`("foo")
        rule = b.build().rule(key)
    }

    @Test
    fun ok() {
        RuleAssert(rule)
            .matches("foo")
            .notMatches("bar")
    }

    @Test
    fun test_matches_failure() {
        val thrown = assertThrows<ParsingResultComparisonFailure> {
            RuleAssert(rule).matches("bar")
        }
        assertTrue(thrown.message.contains("Rule 'ruleName' should match:\nbar"))
    }

    @Test
    fun test_notMatches_failure() {
        val thrown = assertThrows<AssertionError> {
            RuleAssert(rule).notMatches("foo")
        }
        assertEquals(thrown.message, "Rule 'ruleName' should not match:\nfoo")
    }

    @Test
    fun notMatches_should_not_accept_prefix_match() {
        RuleAssert(rule)
            .notMatches("foo bar")
    }

    @Test
    fun matchesPrefix_ok() {
        RuleAssert(rule)
            .matchesPrefix("foo", " bar")
    }

    @Test
    fun matchesPrefix_full_mistmatch() {
        val thrown = assertThrows<ParsingResultComparisonFailure> {
            RuleAssert(rule).matchesPrefix("bar", " baz")
        }
        assertTrue(thrown.message.contains("Rule 'ruleName' should match:\nbar\nwhen followed by:\n baz"))
    }

    @Test
    fun matchesPrefix_wrong_prefix() {
        val thrown = assertThrows<ParsingResultComparisonFailure> {
            RuleAssert(rule).matchesPrefix("foo bar", " baz")
        }
        assertEquals(
            thrown.message,
            "Rule 'ruleName' should match:\nfoo bar\nwhen followed by:\n baz\nbut matched:\nfoo"
        )
    }
}
