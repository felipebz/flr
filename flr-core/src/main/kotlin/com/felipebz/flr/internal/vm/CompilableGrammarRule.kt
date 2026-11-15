//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.internal.matchers.Matcher

/**
 * Rule, which can be compiled by [MutableGrammarCompiler].
 */
public interface CompilableGrammarRule : Matcher, Rule, ParsingExpression {
    public val ruleKey: GrammarRuleKey
    public var expression: ParsingExpression?

    /** The rule creates no AST node: its children are attached to its parent. */
    public fun skip()

    /** The rule creates no AST node when it has exactly one child. */
    public fun skipIfOneChild()
}
