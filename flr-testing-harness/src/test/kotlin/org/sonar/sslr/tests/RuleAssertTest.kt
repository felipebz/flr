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
package org.sonar.sslr.tests

import com.sonar.sslr.api.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.sonar.sslr.internal.grammar.MutableParsingRule

class RuleAssertTest {
    private lateinit var rule: Rule
    @BeforeEach
    fun setUp() {
        rule = MutableParsingRule("ruleName").`is`("foo")
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