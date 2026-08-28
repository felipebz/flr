/**
 * FLR
 * Copyright (C) 2021-2026 Felipe Zorzo
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.parser.ParseRunner
import com.felipebz.flr.parser.ParsingResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParsingSemanticsTest {
    @Test
    fun firstOf_retries_after_an_alternative_partially_consumes_input() {
        val result = parse("ac") { b ->
            b.firstOf(b.sequence("a", "b"), b.sequence("a", "c"))
        }

        assertThat(result.isMatched()).isTrue()
    }

    @Test
    fun firstOf_commits_the_first_successful_alternative() {
        val result = parse("ab") { b ->
            b.sequence(b.firstOf("a", "ab"), b.endOfInput())
        }

        assertThat(result.isMatched()).isFalse()
    }

    @Test
    fun optional_restores_input_after_a_partial_failure_but_does_not_backtrack_after_success() {
        val partialFailure = parse("a") { b ->
            b.sequence(b.optional(b.sequence("a", "b")), "a", b.endOfInput())
        }
        val successfulMatch = parse("a") { b ->
            b.sequence(b.optional("a"), "a", b.endOfInput())
        }

        assertThat(partialFailure.isMatched()).isTrue()
        assertThat(successfulMatch.isMatched()).isFalse()
    }

    @Test
    fun zeroOrMore_restores_input_after_a_partial_failure_but_is_greedy_after_success() {
        val partialFailure = parse("a") { b ->
            b.sequence(b.zeroOrMore(b.sequence("a", "b")), "a", b.endOfInput())
        }
        val successfulMatch = parse("a") { b ->
            b.sequence(b.zeroOrMore("a"), "a", b.endOfInput())
        }

        assertThat(partialFailure.isMatched()).isTrue()
        assertThat(successfulMatch.isMatched()).isFalse()
    }

    @Test
    fun repetition_rejects_a_nullable_body_at_runtime() {
        val zeroOrMoreThrown = assertThrows<GrammarException> {
            parse("") { b -> b.zeroOrMore(b.optional("a")) }
        }
        val oneOrMoreThrown = assertThrows<GrammarException> {
            parse("") { b -> b.oneOrMore(b.optional("a")) }
        }

        assertThat(zeroOrMoreThrown.message)
            .isEqualTo("The inner part of ZeroOrMore and OneOrMore must not allow empty matches")
        assertThat(oneOrMoreThrown.message)
            .isEqualTo("The inner part of ZeroOrMore and OneOrMore must not allow empty matches")
    }

    @Test
    fun parse_runner_does_not_require_end_of_input_without_an_explicit_predicate() {
        val result = parse("ab") { "a" }

        assertThat(result.isMatched()).isTrue()
    }

    @Test
    fun parse_error_does_not_collect_every_failed_primitive_in_a_direct_firstOf() {
        val result = parse("abx") { b ->
            b.firstOf(
                b.sequence("a", "b", "c"),
                b.sequence("a", "d")
            )
        }

        assertThat(result.isMatched()).isFalse()
        assertThat(result.getParseError()).isNotNull
        assertThat(checkNotNull(result.getParseError()).getErrorIndex()).isEqualTo(1)
    }

    @Test
    fun parse_error_keeps_the_farthest_position_seen_while_unwinding_rule_calls() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(DEEP).`is`("a", "b", "c")
        b.rule(ROOT).`is`(
            b.firstOf(
                DEEP,
                b.sequence("a", "d")
            )
        )
        b.setRootRule(ROOT)

        val result = ParseRunner(b.build().rootRule).parse("abx".toCharArray())

        assertThat(result.isMatched()).isFalse()
        assertThat(result.getParseError()).isNotNull
        assertThat(checkNotNull(result.getParseError()).getErrorIndex()).isEqualTo(2)
    }

    private fun parse(input: String, expression: (LexerlessGrammarBuilder) -> Any): ParsingResult {
        val b = LexerlessGrammarBuilder.create()
        b.rule(ROOT).`is`(expression(b))
        b.setRootRule(ROOT)
        return ParseRunner(b.build().rootRule).parse(input.toCharArray())
    }

    private companion object {
        private enum class Root : GrammarRuleKey {
            ROOT, DEEP
        }

        private val ROOT: GrammarRuleKey = Root.ROOT
        private val DEEP: GrammarRuleKey = Root.DEEP
    }
}
