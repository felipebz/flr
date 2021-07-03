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
package org.sonar.sslr.parser

import com.sonar.sslr.api.GenericTokenType
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.parser.GrammarOperators.endOfInput
import org.sonar.sslr.parser.GrammarOperators.nextNot
import org.sonar.sslr.parser.GrammarOperators.skippedTrivia
import org.sonar.sslr.parser.GrammarOperators.token

class ParseRunnerTest {
    @Test
    fun should_report_error_at_rule_level() {
        val rule = MutableParsingRule("rule").`is`("foo", "bar")
        val runner = ParseRunner(rule)
        val result = runner.parse("foo".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(3)
    }

    @Test
    fun should_report_error_at_end_of_input() {
        val endOfInput = MutableParsingRule("endOfInput").`is`(endOfInput())
        val rule = MutableParsingRule("rule").`is`("foo", endOfInput)
        val runner = ParseRunner(rule)
        val result = runner.parse("foo bar".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(3)
    }

    @Test
    fun should_not_report_error_inside_of_predicate_not() {
        val subRule = MutableParsingRule("subRule").`is`("foo")
        val rule = MutableParsingRule("rule").`is`(nextNot(subRule), "bar")
        val runner = ParseRunner(rule)
        val result = runner.parse("baz".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(0)
    }

    @Test
    fun should_report_error_at_correct_index() {
        val rule = MutableParsingRule("rule").`is`(nextNot("foo"))
        val runner = ParseRunner(rule)
        val result = runner.parse("foo".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(0)
    }

    @Test
    fun should_report_error_inside_of_predicate_next() {
        val subRule = MutableParsingRule("subRule").`is`("foo")
        val rule = MutableParsingRule("rule").`is`(GrammarOperators.next(subRule), "bar")
        val runner = ParseRunner(rule)
        val result = runner.parse("baz".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(0)
    }

    @Test
    fun should_not_report_error_inside_of_token() {
        val subRule = MutableParsingRule("subRule").`is`("foo")
        val rule = MutableParsingRule("rule").`is`(token(GenericTokenType.IDENTIFIER, subRule), "bar")
        val runner = ParseRunner(rule)
        val result = runner.parse("baz".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(0)
    }

    @Test
    fun should_not_report_error_inside_of_trivia() {
        val subRule = MutableParsingRule("subRule").`is`("foo")
        val rule = MutableParsingRule("rule").`is`(skippedTrivia(subRule), "bar")
        val runner = ParseRunner(rule)
        val result = runner.parse("baz".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(0)
    }

    @Test
    fun should_report_error_at_several_paths() {
        val subRule1 = MutableParsingRule("subRule1").`is`("foo")
        val subRule2 = MutableParsingRule("subRule2").`is`("bar")
        val rule = MutableParsingRule("rule").`is`(GrammarOperators.firstOf(subRule1, subRule2))
        val runner = ParseRunner(rule)
        val result = runner.parse("baz".toCharArray())
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        println(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(0)
    }
}