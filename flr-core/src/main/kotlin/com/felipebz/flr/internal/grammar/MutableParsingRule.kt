/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.internal.grammar

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeSkippingPolicy
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.impl.ast.AlwaysSkipFromAst
import com.felipebz.flr.impl.ast.NeverSkipFromAst
import com.felipebz.flr.impl.ast.SkipFromAstIfOnlyOneChild
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.*

public class MutableParsingRule : CompilableGrammarRule, Matcher, Rule, AstNodeSkippingPolicy, MemoParsingExpression,
    GrammarRuleKey {
    override val ruleKey: GrammarRuleKey
    private val name: String
    override var expression: ParsingExpression? = null
    private var astNodeSkippingPolicy: AstNodeSkippingPolicy = NeverSkipFromAst

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

    public fun getRealAstNodeType(): AstNodeType {
        return ruleKey
    }

    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#is(Object)} instead.")
    override fun `is`(vararg e: Any): Rule {
        if (expression != null) {
            throw GrammarException("The rule '$ruleKey' has already been defined somewhere in the grammar.")
        }
        expression = sequence(*e)
        return this
    }

    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#override(Object)} instead.")
    override fun override(vararg e: Any): Rule {
        expression = sequence(*e)
        return this
    }

    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#skip()} instead.")
    override fun skip() {
        astNodeSkippingPolicy = AlwaysSkipFromAst
    }

    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#skipIfOneChild()} instead.")
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
        return getName()
    }

    override fun shouldMemoize(): Boolean {
        return true
    }

    private fun sequence(vararg e: Any): ParsingExpression {
        return convertToSingleExpression(*e)
    }

    private fun convertToSingleExpression(vararg elements: Any): ParsingExpression {
        return if (elements.size == 1) {
            convertToExpression(elements[0])
        } else SequenceExpression(*convertToExpressions(*elements))
    }

    private fun convertToExpressions(vararg elements: Any): Array<ParsingExpression> {
        require(elements.isNotEmpty())
        val matchers = arrayOfNulls<ParsingExpression>(elements.size)
        for (i in matchers.indices) {
            matchers[i] = convertToExpression(elements[i])
        }
        return matchers.requireNoNulls()
    }

    private fun convertToExpression(e: Any): ParsingExpression {
        return when (e) {
            is ParsingExpression -> {
                e
            }
            is String -> {
                StringExpression(e)
            }
            is Char -> {
                StringExpression(e.toString())
            }
            else -> {
                throw IllegalArgumentException("Incorrect type of parsing expression: " + e.javaClass.toString())
            }
        }
    }
}
