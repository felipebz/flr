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
 * While for performance reasons preferable to write greedy rules instead of recursive,
 * impossible to avoid recursion at all.
 * Depth of recursion not limited by Java stack size (i.e. can't lead to [StackOverflowError])
 * and limited only by amount of available memory.
 */
public enum class RecursiveGrammar : GrammarRuleKey {
    S;

    public companion object {
        @JvmStatic
        public fun create(): Grammar {
            val b = LexerlessGrammarBuilder.create()
            b.rule(S).`is`("(", b.optional(S), ")")
            return b.build()
        }
    }
}
