/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.internal.grammar

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeSkippingPolicy
import com.sonar.sslr.api.AstNodeType
import com.sonar.sslr.api.Rule
import com.sonar.sslr.impl.ast.AlwaysSkipFromAst
import com.sonar.sslr.impl.ast.NeverSkipFromAst
import com.sonar.sslr.impl.ast.SkipFromAstIfOnlyOneChild
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.matchers.Matcher
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.parser.GrammarOperators

class MutableParsingRule : CompilableGrammarRule, Matcher, Rule, AstNodeSkippingPolicy, MemoParsingExpression,
    GrammarRuleKey {
    override val ruleKey: GrammarRuleKey
    private val name: String
    override var expression: ParsingExpression? = null
    private var astNodeSkippingPolicy: AstNodeSkippingPolicy = NeverSkipFromAst.INSTANCE

    constructor(name: String) {
        ruleKey = this
        this.name = name
    }

    constructor(ruleKey: GrammarRuleKey) {
        this.ruleKey = ruleKey
        name = ruleKey.toString()
    }

    fun getName(): String {
        return name
    }

    fun getRealAstNodeType(): AstNodeType {
        return ruleKey
    }

    override fun `is`(vararg e: Any): Rule {
        if (expression != null) {
            throw GrammarException("The rule '$ruleKey' has already been defined somewhere in the grammar.")
        }
        expression = GrammarOperators.sequence(*e) as ParsingExpression
        return this
    }

    override fun override(vararg e: Any): Rule {
        expression = GrammarOperators.sequence(*e) as ParsingExpression
        return this
    }

    override fun mock() {
        expression =
            SequenceExpression(
                StringExpression(getName()),
                FirstOfExpression(
                    PatternExpression("\\s++"),
                    EndOfInputExpression.INSTANCE
                )
            )
    }

    override fun skip() {
        astNodeSkippingPolicy = AlwaysSkipFromAst.INSTANCE
    }

    override fun skipIfOneChild() {
        astNodeSkippingPolicy = SkipFromAstIfOnlyOneChild.INSTANCE
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
}