/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2026 Felipe Zorzo
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
package com.felipebz.flr.api

import com.felipebz.flr.grammar.GrammarRuleKey

/**
 * Use [com.felipebz.flr.grammar.LexerfulGrammarBuilder] or [com.felipebz.flr.grammar.LexerlessGrammarBuilder] to create
 * instances of this class.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public abstract class Grammar {
    /**
     * Allows to obtain an instance of grammar rule, which was constructed by
     * [com.felipebz.flr.grammar.LexerlessGrammarBuilder] and [com.felipebz.flr.grammar.LexerfulGrammarBuilder].
     */
    public open fun rule(ruleKey: GrammarRuleKey): Rule {
        throw UnsupportedOperationException()
    }

    /**
     * Each Grammar has always an entry point whose name is usually by convention the "Computation Unit".
     *
     * @return the entry point of this Grammar
     */
    public abstract val rootRule: Rule
}
