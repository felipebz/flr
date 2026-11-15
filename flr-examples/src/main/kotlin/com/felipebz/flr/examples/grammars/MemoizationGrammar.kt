//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.api.Grammar
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder.Companion.create

public enum class MemoizationGrammar : GrammarRuleKey {
    A, B, C;

    public companion object {
        @JvmStatic
        public fun requiresNegativeMemoization(): Grammar {
            val b = create()
            b.rule(A).`is`(
                'a',
                b.firstOf(
                    b.sequence(A, 'b'),
                    b.sequence(A, 'c')
                )
            )
            return b.build()
        }

        @JvmStatic
        public fun requiresPositiveMemoization(): Grammar {
            val b = create()
            b.rule(A).`is`(
                b.firstOf(
                    b.sequence(b.optional(B), 'a'),
                    b.sequence(b.optional(B), 'b')
                )
            )
            b.rule(B).`is`('(', A, ')')
            return b.build()
        }

        @JvmStatic
        public fun requiresPositiveMemoizationOnMoreThanJustLastRule(): Grammar {
            val b = create()
            b.rule(A).`is`(
                b.firstOf(
                    b.sequence(b.optional(B), 'a'),  // rule 'C' will match and override the memoization result of 'B':
                    b.sequence(C, '!'),
                    b.sequence(b.optional(B), 'b')
                )
            )
            b.rule(B).`is`('(', A, ')')
            // rule 'C' will override each following memoization result of 'A':
            b.rule(C).`is`('(', b.optional(C))
            return b.build()
        }
    }
}
