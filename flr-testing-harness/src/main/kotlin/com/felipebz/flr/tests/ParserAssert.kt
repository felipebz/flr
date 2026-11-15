//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.internal.vm.EndOfInputExpression
import com.felipebz.flr.internal.vm.FirstOfExpression
import com.felipebz.flr.internal.vm.lexerful.TokenTypeExpression
import com.felipebz.flr.tests.Assertions.assertThat
import com.felipebz.flr.tests.RuleAssert.WithEndOfInput
import org.assertj.core.api.AbstractAssert

/**
 * To create a new instance of this class invoke `[assertThat]`.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class ParserAssert(actual: Parser<*>) : AbstractAssert<ParserAssert, Parser<*>>(
    actual, ParserAssert::class.java
) {
    private fun createParserWithEofMatcher(): Parser<*> {
        val rule = actual.rootRule
        val cache = eofParsers.get()
        cache[actual]?.let { (cachedRule, parser) -> if (cachedRule === rule) return parser }
        val builder = LexerfulGrammarBuilder.create()
        val withEndOfInputKey = WithEndOfInput(rule.ruleKey)
        builder.rule(withEndOfInputKey).`is`(rule, FirstOfExpression(EndOfInputExpression, TokenTypeExpression(GenericTokenType.EOF)))
        builder.setRootRule(withEndOfInputKey)
        val parser: Parser<*> = Parser.builder(actual).build()
        parser.setRootRule(builder.build().rootRule)
        cache[actual] = rule to parser
        return parser
    }

    /**
     * Verifies that the actual `[Parser]` fully matches a given input.
     * @return this assertion object.
     */
    public fun matches(input: String): ParserAssert {
        isNotNull
        hasRootRule()
        val parser = createParserWithEofMatcher()
        val expected = """
             Rule '${getRuleName()}' should match:
             $input
             """.trimIndent()
        try {
            parser.parse(input)
        } catch (e: RecognitionException) {
            val actual = e.message
            throw ParsingResultComparisonFailure(expected, actual)
        }
        return this
    }

    /**
     * Verifies that the actual `[Parser]` not matches a given input.
     * @return this assertion object.
     */
    public fun notMatches(input: String): ParserAssert {
        isNotNull
        hasRootRule()
        val parser = createParserWithEofMatcher()
        try {
            parser.parse(input)
        } catch (e: RecognitionException) {
            // expected
            return this
        }
        throw AssertionError(
            """
    Rule '${getRuleName()}' should not match:
    $input
    """.trimIndent()
        )
    }

    private fun hasRootRule() {
        assertThat(actual.rootRule)
            .overridingErrorMessage("Root rule of the parser should not be null")
            .isNotNull
    }

    private fun getRuleName(): String {
        return actual.rootRule.name
    }

    private companion object {
        val eofParsers: ThreadLocal<MutableMap<Parser<*>, Pair<Any, Parser<*>>>> =
            ThreadLocal.withInitial { java.util.WeakHashMap() }
    }
}
