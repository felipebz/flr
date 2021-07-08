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
package com.sonar.sslr.impl.matcher

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeSkippingPolicy
import com.sonar.sslr.api.AstNodeType
import com.sonar.sslr.api.Rule
import com.sonar.sslr.impl.ast.AlwaysSkipFromAst
import com.sonar.sslr.impl.ast.NeverSkipFromAst
import com.sonar.sslr.impl.ast.SkipFromAstIfOnlyOneChild
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.vm.*
import java.util.*

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class RuleDefinition : Rule, AstNodeSkippingPolicy, GrammarRuleKey, CompilableGrammarRule, MemoParsingExpression {
    override val ruleKey: GrammarRuleKey
    private val name: String
    override var expression: ParsingExpression? = null
    private var astNodeSkippingPolicy: AstNodeType = NeverSkipFromAst.INSTANCE
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

    override fun `is`(vararg e: Any): RuleDefinition {
        throwExceptionIfRuleAlreadyDefined("The rule '$ruleKey' has already been defined somewhere in the grammar.")
        throwExceptionIfEmptyListOfMatchers(e)
        expression = GrammarFunctions.convertToSingleExpression(e)
        return this
    }

    override fun override(vararg e: Any): RuleDefinition {
        throwExceptionIfEmptyListOfMatchers(e)
        expression = GrammarFunctions.convertToSingleExpression(e)
        return this
    }

    override fun mock() {
        expression = GrammarFunctions.Standard.firstOf(getName(), getName().uppercase(Locale.getDefault())) as ParsingExpression
    }

    override fun skip() {
        astNodeSkippingPolicy = AlwaysSkipFromAst.INSTANCE
    }

    override fun skipIfOneChild() {
        astNodeSkippingPolicy = SkipFromAstIfOnlyOneChild.INSTANCE
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
}