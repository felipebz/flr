//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.api.Grammar
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder.Companion.create

/**
 * This class demonstrates how SSLR detects various mistakes in grammars.
 */
public enum class IncorrectGrammar : GrammarRuleKey {
    A, B;

    public companion object {
        @JvmStatic
        public fun undefinedRule(): Grammar {
            val b = create()
            b.rule(A)
            return b.build()
        }

        @JvmStatic
        public fun referenceToUndefinedRule(): Grammar {
            val b = create()
            b.rule(A).`is`(B)
            return b.build()
        }

        @JvmStatic
        public fun ruleDefinedTwice(): Grammar {
            val b = create()
            b.rule(A).`is`("foo")
            b.rule(A).`is`("bar")
            return b.build()
        }

        @JvmStatic
        public fun incorrectRegularExpression(): Grammar {
            val b = create()
            b.rule(A).`is`(b.regexp("*"))
            return b.build()
        }

        @JvmStatic
        public fun infiniteZeroOrMore(): Grammar {
            val b = create()
            b.rule(A).`is`(b.zeroOrMore(b.optional("foo")))
            return b.build()
        }

        @JvmStatic
        public fun infiniteOneOrMore(): Grammar {
            val b = create()
            b.rule(A).`is`(b.oneOrMore(b.optional("foo")))
            return b.build()
        }
    }
}
