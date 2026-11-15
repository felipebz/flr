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
 * Grammar for the classic non-context-free language { a^n b^n c^n : n >= 1 }.
 */
public enum class AbcGrammar : GrammarRuleKey {
    S, A, B;

    public companion object {
        @JvmStatic
        public fun createGrammar(): Grammar {
            val b = create()
            b.rule(S).`is`(b.next(A, "c"), b.oneOrMore("a"), B, b.nextNot("a", "b", "c"))
            b.rule(A).`is`("a", b.optional(A), "b")
            b.rule(B).`is`("b", b.optional(B), "c")
            return b.build()
        }
    }
}
