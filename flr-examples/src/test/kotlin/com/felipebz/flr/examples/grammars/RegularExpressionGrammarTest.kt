//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class RegularExpressionGrammarTest {
    private val g = RegularExpressionGrammar.create()
    @Test
    fun character_class() {
        assertThat(g.rule(RegularExpressionGrammar.CHARACTER_CLASS)) /* simple class */
            .matches(correct("[abc]"))
            .matches(correct("[&]"))
            .matches(correct("[\\^]"))
            .matches(correct("[\\]]"))
            .matches(correct("[\\[]")) /* negation */
            .matches(correct("[^abc]"))
            .matches(correct("[^^]")) /* range */
            .matches(correct("[a-z]"))
            .matches(correct("[a-zA-Z]")) /* union */
            .matches(correct("[a-d[m-p]]"))
            .matches(correct("[[a-z]]")) /* intersection */
            .matches(correct("[a-z&&[def]]"))
            .matches(correct("[&&[a-z]]")) /* subtraction */
            .matches(correct("[a-z&&[^bc]]"))
            .matches(correct("[a-z&&[^m-p]]")) /* incorrect */
            .notMatches(incorrect("[]"))
            .notMatches(incorrect("[\\]"))
            .notMatches(incorrect("[[]"))
            .notMatches(incorrect("[a[]"))
            .notMatches(incorrect("[^]"))
            .notMatches(incorrect("[&&]"))
    }

    @Test
    fun atom() {
        assertThat(g.rule(RegularExpressionGrammar.ATOM)) /* character */
            .matches(correct("a"))
            .matches(correct("]")) /* character class */
            .matches(correct("[a-z]")) /* any character */
            .matches(correct(".")) /* back reference */
            .matches(correct("\\1")) /* incorrect */
            .notMatches(incorrect("\\0"))
            .notMatches(incorrect("["))
            .notMatches(incorrect("("))
            .notMatches(incorrect(")"))
            .notMatches(incorrect("*"))
            .notMatches(incorrect("+"))
            .notMatches(incorrect("?"))
            .notMatches("|")
    }

    @Test
    fun term() {
        assertThat(g.rule(RegularExpressionGrammar.TERM)) /* atom */
            .matches(correct("a")) /* atom with quantifier */
            .matches(correct("a++")) /* capturing group */
            .matches(correct("()")) /* non-capturing group */
            .matches(correct("(?:)")) /* zero-width positive lookahead */
            .matches(correct("(?=)")) /* zero-width negative lookahead */
            .matches(correct("(?!)"))
    }

    @Test
    fun alternation() {
        assertThat(g.rule(RegularExpressionGrammar.ALTERNATION))
            .matches("")
            .matches(correct("a|b"))
            .matches(correct("a|b|c"))
            .matches(correct("|"))
    }

    @Test
    fun quantifier() {
        assertThat(g.rule(RegularExpressionGrammar.QUANTIFIER)) /* greedy */
            .matches("*")
            .matches("+")
            .matches("?")
            .matches("{1}")
            .matches("{1,}")
            .matches("{1,2}") /* reluctant */
            .matches("*?")
            .matches("+?")
            .matches("??")
            .matches("{1}?")
            .matches("{1,}?")
            .matches("{1,2}?") /* possessive */
            .matches("*+")
            .matches("++")
            .matches("?+")
            .matches("{1}+")
            .matches("{1,}+")
            .matches("{1,2}+")
    }

    companion object {
        private fun incorrect(pattern: String): String {
            return try {
                Pattern.compile(pattern)
                throw AssertionError(pattern)
            } catch (e: PatternSyntaxException) {
                pattern
            }
        }

        private fun correct(pattern: String): String {
            return Pattern.compile(pattern).pattern()
        }
    }
}
