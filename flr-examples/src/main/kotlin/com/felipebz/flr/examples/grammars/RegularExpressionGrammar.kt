//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.api.Grammar
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder

/**
 * Regular Expression Grammar.
 */
public enum class RegularExpressionGrammar : GrammarRuleKey {
    EXPRESSION, ALTERNATION, ALTERNATIVE, TERM, CAPTURING_GROUP, NON_CAPTURING_GROUP, POSITIVE_LOOKAHEAD, NEGATIVE_LOOKAHEAD, QUANTIFIER, NUMBER, ATOM, BACK_REFERENCE, CHARACTER_CLASS, CLASS_ATOM;

    public companion object {
        @JvmStatic
        public fun create(): Grammar {
            val b = LexerlessGrammarBuilder.create()
            b.rule(EXPRESSION).`is`(ALTERNATION)
            b.rule(ALTERNATION).`is`(ALTERNATIVE, b.zeroOrMore("|", ALTERNATIVE))
            b.rule(ALTERNATIVE).`is`(b.zeroOrMore(TERM))
            b.rule(TERM).`is`(
                b.firstOf(ATOM, POSITIVE_LOOKAHEAD, NEGATIVE_LOOKAHEAD, NON_CAPTURING_GROUP, CAPTURING_GROUP),
                b.optional(QUANTIFIER)
            )
            b.rule(POSITIVE_LOOKAHEAD).`is`("(?=", ALTERNATION, ")")
            b.rule(NEGATIVE_LOOKAHEAD).`is`("(?!", ALTERNATION, ")")
            b.rule(CAPTURING_GROUP).`is`("(", ALTERNATION, ")")
            b.rule(NON_CAPTURING_GROUP).`is`("(?:", ALTERNATION, ")")
            b.rule(QUANTIFIER).`is`(
                b.firstOf(
                    "?",
                    "*",
                    "+",
                    b.sequence("{", NUMBER, "}"),
                    b.sequence("{", NUMBER, ",", "}"),
                    b.sequence("{", NUMBER, ",", NUMBER, "}")
                ),
                b.optional(b.firstOf("+", "?"))
            )
            b.rule(NUMBER).`is`(b.regexp("[0-9]++"))
            b.rule(ATOM).`is`(
                b.firstOf(
                    ".", BACK_REFERENCE,
                    b.sequence(b.nextNot(b.firstOf('[', '*', '+', '?', '(', ')', '|')), b.regexp(".")), CHARACTER_CLASS
                )
            )
            b.rule(BACK_REFERENCE).`is`("\\", b.regexp("[1-9]"))
            b.rule(CHARACTER_CLASS).`is`('[', b.optional("^"), b.oneOrMore(CLASS_ATOM), ']')
            b.rule(CLASS_ATOM).`is`(
                b.firstOf(
                    b.sequence('\\', b.regexp(".")),
                    b.sequence(b.optional("&&"), CHARACTER_CLASS),
                    b.sequence(b.nextNot(b.firstOf('[', ']', "&&")), b.regexp("."))
                )
            )
            return b.build()
        }
    }
}
