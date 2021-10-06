/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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