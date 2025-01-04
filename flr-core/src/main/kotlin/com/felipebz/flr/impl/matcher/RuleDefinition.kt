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
package com.felipebz.flr.impl.matcher

import com.felipebz.flr.api.*
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.impl.ast.AlwaysSkipFromAst
import com.felipebz.flr.impl.ast.NeverSkipFromAst
import com.felipebz.flr.impl.ast.SkipFromAstIfOnlyOneChild
import com.felipebz.flr.internal.vm.*
import com.felipebz.flr.internal.vm.lexerful.TokenTypeClassExpression
import com.felipebz.flr.internal.vm.lexerful.TokenTypeExpression
import com.felipebz.flr.internal.vm.lexerful.TokenValueExpression

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

    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#is(Object)} instead.")
    override fun `is`(vararg e: Any): RuleDefinition {
        throwExceptionIfRuleAlreadyDefined("The rule '$ruleKey' has already been defined somewhere in the grammar.")
        throwExceptionIfEmptyListOfMatchers(e)
        expression = convertToSingleExpression(e)
        return this
    }

    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#override(Object)} instead.")
    override fun override(vararg e: Any): RuleDefinition {
        throwExceptionIfEmptyListOfMatchers(e)
        expression = convertToSingleExpression(e)
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

    private fun throwExceptionIfRuleAlreadyDefined(exceptionMessage: String) {
        check(expression == null) { exceptionMessage }
    }

    private fun throwExceptionIfEmptyListOfMatchers(matchers: Array<out Any>) {
        check(matchers.isNotEmpty()) { "The rule '$ruleKey' should at least contains one matcher." }
    }

    override fun hasToBeSkippedFromAst(node: AstNode): Boolean {
        return if (AstNodeSkippingPolicy::class.java.isAssignableFrom(astNodeSkippingPolicy.javaClass)) {
            (astNodeSkippingPolicy as AstNodeSkippingPolicy).hasToBeSkippedFromAst(node)
        } else false
    }

    /**
     * @since 1.18
     */
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

    private fun convertToSingleExpression(e: Array<out Any>): ParsingExpression {
        checkSize(e)
        return if (e.size == 1) {
            convertToExpression(e[0])
        } else {
            SequenceExpression(*convertToExpressions(e))
        }
    }

    private fun convertToExpressions(e: Array<out Any>): Array<out ParsingExpression> {
        checkSize(e)
        val matchers = arrayOfNulls<ParsingExpression>(e.size)
        for (i in matchers.indices) {
            matchers[i] = convertToExpression(e[i])
        }
        return matchers.requireNoNulls()
    }

    private fun convertToExpression(e: Any): ParsingExpression {
        return when (e) {
            is String -> {
                TokenValueExpression(e)
            }
            is TokenType -> {
                TokenTypeExpression(e)
            }
            is RuleDefinition -> {
                e
            }
            is Class<*> -> {
                TokenTypeClassExpression(e)
            }
            is ParsingExpression -> {
                e
            }
            else -> {
                throw IllegalArgumentException("The matcher object can't be anything else than a Rule, Matcher, String, TokenType or Class. Object = $e")
            }
        }
    }

    private fun checkSize(e: Array<out Any>) {
        require(e.isNotEmpty()) { "You must define at least one matcher." }
    }
}
