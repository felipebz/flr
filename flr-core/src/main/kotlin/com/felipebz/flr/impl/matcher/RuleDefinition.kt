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
package com.felipebz.flr.impl.matcher

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeSkippingPolicy
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.impl.ast.AlwaysSkipFromAst
import com.felipebz.flr.impl.ast.NeverSkipFromAst
import com.felipebz.flr.impl.ast.SkipFromAstIfOnlyOneChild
import com.felipebz.flr.internal.vm.*

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class RuleDefinition : Rule, AstNodeSkippingPolicy, GrammarRuleKey, CompilableGrammarRule, MemoParsingExpression {
    override val ruleKey: GrammarRuleKey
    private val name: String
    override var expression: ParsingExpression? = null
    private var astNodeSkippingPolicy: AstNodeType = NeverSkipFromAst
    private var memoize = false
    private var retainMemo = false

    public constructor(name: String) {
        ruleKey = this
        this.name = name
    }

    public constructor(ruleKey: GrammarRuleKey) {
        this.ruleKey = ruleKey
        name = ruleKey.toString()
    }

    public fun getName(): String {
        return name
    }

    override fun skip() {
        astNodeSkippingPolicy = AlwaysSkipFromAst
    }

    override fun skipIfOneChild() {
        astNodeSkippingPolicy = SkipFromAstIfOnlyOneChild
    }

    override fun hasToBeSkippedFromAst(node: AstNode): Boolean {
        return if (AstNodeSkippingPolicy::class.java.isAssignableFrom(astNodeSkippingPolicy.javaClass)) {
            (astNodeSkippingPolicy as AstNodeSkippingPolicy).hasToBeSkippedFromAst(node)
        } else false
    }

    internal fun isAlwaysSkipFromAst(): Boolean {
        return astNodeSkippingPolicy === AlwaysSkipFromAst
    }

    internal fun isSkipIfOneChildFromAst(): Boolean {
        return astNodeSkippingPolicy === SkipFromAstIfOnlyOneChild
    }

    public fun getRealAstNodeType(): AstNodeType {
        return ruleKey
    }

    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        return compiler.compile(RuleRefExpression(ruleKey))
    }

    override fun toString(): String {
        return getName()
    }

    override fun shouldMemoize(): Boolean {
        return memoize
    }

    public fun enableMemoization() {
        memoize = true
    }

    override fun shouldRetainMemo(): Boolean {
        return retainMemo
    }

    internal fun enableMemoRetention() {
        memoize = true
        retainMemo = true
    }
}
