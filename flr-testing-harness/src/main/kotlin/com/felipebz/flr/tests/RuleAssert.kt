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
package com.felipebz.flr.tests

import com.felipebz.flr.api.Rule
import org.fest.assertions.GenericAssert
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.EndOfInputExpression
import com.felipebz.flr.parser.ParseErrorFormatter
import com.felipebz.flr.parser.ParseRunner

/**
 * To create a new instance of this class invoke `[Assertions.assertThat]`.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 *
 * @since 1.16
 */
public class RuleAssert(actual: Rule?) : GenericAssert<RuleAssert, Rule>(
    RuleAssert::class.java, actual
) {
    internal class WithEndOfInput(private val ruleKey: GrammarRuleKey?) : GrammarRuleKey {
        override fun toString(): String {
            return ruleKey.toString() + " with end of input"
        }
    }

    private fun createParseRunnerWithEofMatcher(): ParseRunner {
        isNotNull
        val rule = actual as MutableParsingRule

        val builder = LexerlessGrammarBuilder.create()
        val withEndOfInputKey = WithEndOfInput(rule.ruleKey)
        builder.rule(withEndOfInputKey).`is`(actual, EndOfInputExpression.INSTANCE)
        builder.setRootRule(withEndOfInputKey)
        return ParseRunner(builder.build().rootRule)
    }

    /**
     * Verifies that the actual `[Rule]` fully matches a given input.
     * @return this assertion object.
     */
    public fun matches(input: String): RuleAssert {
        val parseRunner = createParseRunnerWithEofMatcher()
        val parsingResult = parseRunner.parse(input.toCharArray())
        if (!parsingResult.isMatched()) {
            val expected = """
                Rule '${getRuleName()}' should match:
                $input
                """.trimIndent()
            val actual = ParseErrorFormatter().format(parsingResult.getParseError())
            throw ParsingResultComparisonFailure(expected, actual)
        }
        return this
    }

    /**
     * Verifies that the actual `[Rule]` does not match a given input.
     * @return this assertion object.
     */
    public fun notMatches(input: String): RuleAssert {
        val parseRunner = createParseRunnerWithEofMatcher()
        val parsingResult = parseRunner.parse(input.toCharArray())
        if (parsingResult.isMatched()) {
            throw AssertionError(
                """
    Rule '${getRuleName()}' should not match:
    $input
    """.trimIndent()
            )
        }
        return this
    }

    private fun createParseRunnerWithoutEofMatcher(): ParseRunner {
        isNotNull
        return ParseRunner(actual)
    }

    /**
     * Verifies that the actual `[Rule]` partially matches a given input.
     * @param prefixToBeMatched the prefix that must be fully matched
     * @param remainingInput the remainder of the input, which is not to be matched
     * @return this assertion object.
     */
    public fun matchesPrefix(prefixToBeMatched: String, remainingInput: String): RuleAssert {
        val parseRunner = createParseRunnerWithoutEofMatcher()
        val input = prefixToBeMatched + remainingInput
        val parsingResult = parseRunner.parse(input.toCharArray())
        if (!parsingResult.isMatched()) {
            val expected = """
                Rule '${getRuleName()}' should match:
                $prefixToBeMatched
                when followed by:
                $remainingInput
                """.trimIndent()
            val actual = ParseErrorFormatter().format(parsingResult.getParseError())
            throw ParsingResultComparisonFailure(expected, actual)
        } else if (prefixToBeMatched.length != parsingResult.getParseTreeRoot().endIndex) {
            val actualMatchedPrefix = input.substring(0, parsingResult.getParseTreeRoot().endIndex)
            val message = """
                Rule '${getRuleName()}' should match:
                $prefixToBeMatched
                when followed by:
                $remainingInput
                but matched:
                $actualMatchedPrefix
                """.trimIndent()
            throw ParsingResultComparisonFailure(message, prefixToBeMatched, actualMatchedPrefix)
        }
        return this
    }

    private fun getRuleName(): String {
        return (actual as MutableParsingRule).getName()
    }
}