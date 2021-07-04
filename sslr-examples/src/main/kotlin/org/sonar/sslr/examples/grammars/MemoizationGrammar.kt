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
package org.sonar.sslr.examples.grammars

import com.sonar.sslr.api.Grammar
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.grammar.LexerlessGrammarBuilder.Companion.create

enum class MemoizationGrammar : GrammarRuleKey {
    A, B, C;

    companion object {
        @JvmStatic
        fun requiresNegativeMemoization(): Grammar {
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
        fun requiresPositiveMemoization(): Grammar {
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
        fun requiresPositiveMemoizationOnMoreThanJustLastRule(): Grammar {
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