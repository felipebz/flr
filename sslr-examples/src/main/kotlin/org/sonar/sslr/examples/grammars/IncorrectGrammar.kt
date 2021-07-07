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