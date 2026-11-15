//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.EndOfInputExpression
import com.felipebz.flr.parser.ParseErrorFormatter
import com.felipebz.flr.parser.ParseRunner
import org.assertj.core.api.AbstractAssert

/**
 * To create a new instance of this class invoke `[Assertions.assertThat]`.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class RuleAssert(actual: Rule?) : AbstractAssert<RuleAssert, Rule>(
    actual, RuleAssert::class.java
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
        builder.rule(withEndOfInputKey).`is`(actual, EndOfInputExpression)
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
        return (actual as MutableParsingRule).name
    }
}
