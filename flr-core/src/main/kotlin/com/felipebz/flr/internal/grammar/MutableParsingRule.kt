//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.grammar

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeSkippingPolicy
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.impl.ast.AlwaysSkipFromAst
import com.felipebz.flr.impl.ast.NeverSkipFromAst
import com.felipebz.flr.impl.ast.SkipFromAstIfOnlyOneChild
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.*

public class MutableParsingRule : CompilableGrammarRule, Matcher, Rule, AstNodeSkippingPolicy, MemoParsingExpression,
    GrammarRuleKey {
    override val ruleKey: GrammarRuleKey
    public val name: String
    override var expression: ParsingExpression? = null
    private var astNodeSkippingPolicy: AstNodeSkippingPolicy = NeverSkipFromAst
    private var retainMemo = false

    public constructor(name: String) {
        ruleKey = this
        this.name = name
    }

    public constructor(ruleKey: GrammarRuleKey) {
        this.ruleKey = ruleKey
        name = ruleKey.toString()
    }

    public fun getRealAstNodeType(): AstNodeType {
        return ruleKey
    }

    override fun skip() {
        astNodeSkippingPolicy = AlwaysSkipFromAst
    }

    override fun skipIfOneChild() {
        astNodeSkippingPolicy = SkipFromAstIfOnlyOneChild
    }

    override fun hasToBeSkippedFromAst(node: AstNode): Boolean {
        return astNodeSkippingPolicy.hasToBeSkippedFromAst(node)
    }

    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        return compiler.compile(RuleRefExpression(ruleKey))
    }

    override fun toString(): String {
        return name
    }

    override fun shouldMemoize(): Boolean {
        return true
    }

    override fun shouldRetainMemo(): Boolean {
        return retainMemo
    }

    internal fun enableMemoRetention() {
        retainMemo = true
    }
}
