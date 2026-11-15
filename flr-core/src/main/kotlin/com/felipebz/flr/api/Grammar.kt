//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
