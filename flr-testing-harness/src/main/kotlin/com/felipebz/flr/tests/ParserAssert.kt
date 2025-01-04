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
 *
 * @since 1.16
 */
public class ParserAssert(actual: Parser<*>) : AbstractAssert<ParserAssert, Parser<*>>(
    actual, ParserAssert::class.java
) {
    private fun createParserWithEofMatcher(): Parser<*> {
        val rule = actual.rootRule
        val builder = LexerfulGrammarBuilder.create()
        val withEndOfInputKey = WithEndOfInput(rule.ruleKey)
        builder.rule(withEndOfInputKey).`is`(rule, FirstOfExpression(EndOfInputExpression, TokenTypeExpression(GenericTokenType.EOF)))
        builder.setRootRule(withEndOfInputKey)
        val parser: Parser<*> = Parser.builder(actual).build()
        parser.setRootRule(builder.build().rootRule)
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
        return actual.rootRule.getName()
    }
}
