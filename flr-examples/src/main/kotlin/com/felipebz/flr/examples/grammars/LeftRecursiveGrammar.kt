/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.examples.grammars

import com.felipebz.flr.api.Grammar
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.grammar.LexerlessGrammarBuilder.Companion.create

/**
 * Support of left recursion in PEG is controversial and not well defined ([Bryan Ford](http://comments.gmane.org/gmane.comp.parsers.peg.general/429)),
 * as there are papers claiming that it is easily achieved
 * ([Packrat Parsers Can Support Left Recursion, Warth et al.](http://www.tinlizzie.org/~awarth/papers/pepm08.pdf)),
 * and others that it is much harder
 * ([Direct Left-Recursive Parsing Expressing Grammars, Laurence Tratt](http://port70.net/~nsz/articles/other/tratt_direct_left_recursive_peg_2010.pdf)).
 * For those reasons, it was decided not to support it - SSLR can detect existence of left recursion at runtime and will report this as an error.
 */
public enum class LeftRecursiveGrammar : GrammarRuleKey {
    A, B, T1, T2, S1, S2;

    public companion object {
        /**
         * @see .eliminatedImmediateLeftRecursion
         */
        @JvmStatic
        public fun immediateLeftRecursion(): Grammar {
            val b = create()
            b.rule(A).`is`(
                b.firstOf(
                    b.sequence(A, T1),
                    b.sequence(A, T2), S1, S2
                )
            )
            otherRules(b)
            return b.build()
        }

        /**
         * To eliminate immediate left recursion - factor out non recursive alternatives.
         */
        @JvmStatic
        public fun eliminatedImmediateLeftRecursion(): Grammar {
            val b = create()
            b.rule(A).`is`(b.firstOf(S1, S2), b.zeroOrMore(b.firstOf(T1, T2)))
            otherRules(b)
            return b.build()
        }

        /**
         * @see .eliminatedIndirectLeftRecursion
         */
        @JvmStatic
        public fun indirectLeftRecursion(): Grammar {
            val b = create()
            b.rule(A).`is`(
                b.firstOf(
                    b.sequence(B, T1), S1
                )
            )
            b.rule(B).`is`(
                b.firstOf(
                    b.sequence(A, T2), S2
                )
            )
            otherRules(b)
            return b.build()
        }

        /**
         * To eliminate indirect left recursion - transform to immediate left recursion, then factor out non recursive alternatives.
         */
        @JvmStatic
        public fun eliminatedIndirectLeftRecursion(): Grammar {
            val b = create()
            b.rule(A).`is`(b.firstOf(b.sequence(S2, T1), S1), b.zeroOrMore(T2, T1))
            otherRules(b)
            return b.build()
        }

        private fun otherRules(b: LexerlessGrammarBuilder) {
            b.rule(T1).`is`("t1")
            b.rule(T2).`is`("t2")
            b.rule(S1).`is`("s1")
            b.rule(S2).`is`("s2")
        }
    }
}