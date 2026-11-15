//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.typed

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.typed.GrammarBuilder
import com.felipebz.flr.api.typed.NonterminalBuilder
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.internal.vm.FirstOfExpression
import com.felipebz.flr.internal.vm.ParsingExpression
import com.felipebz.flr.internal.vm.SequenceExpression
import java.lang.reflect.Method
import java.util.*

public class GrammarBuilderInterceptor<T>(private val b: LexerlessGrammarBuilder) : MethodInterceptor,
    GrammarBuilder<T?>, NonterminalBuilder<Any?> {
    private val mappedRuleKeys: MutableSet<GrammarRuleKey> = HashSet()
    private val methodToRuleKey: MutableMap<Method?, GrammarRuleKey?> = HashMap()
    private val actions: MutableMap<GrammarRuleKey, Method> = HashMap()
    private val optionals: MutableSet<GrammarRuleKey> = HashSet()
    private val oneOrMores: MutableSet<GrammarRuleKey> = HashSet()
    private val zeroOrMores: MutableSet<GrammarRuleKey> = HashSet()
    private var buildingMethod: Method? = null
    private var ruleKey: GrammarRuleKey? = null
    private val expressionStack: Deque<ParsingExpression> = ArrayDeque()
    override fun intercept(method: Method): Boolean {
        if (buildingMethod != null) {
            push(DelayedRuleInvocationExpression(b, this, method))
            return true
        }
        buildingMethod = method
        return false
    }

    override fun <U> nonterminal(): NonterminalBuilder<U> {
        return nonterminal(DummyGrammarRuleKey(buildingMethod))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <U> nonterminal(ruleKey: GrammarRuleKey): NonterminalBuilder<U> {
        this.ruleKey = ruleKey
        mappedRuleKeys.add(ruleKey)
        methodToRuleKey[buildingMethod] = this.ruleKey
        return this as NonterminalBuilder<U>
    }

    override fun `is`(method: Any?): Any? {
        check(expressionStack.size == 1) { "Unexpected stack size: " + expressionStack.size }
        val expression = pop()
        b.rule(checkNotNull(ruleKey)).`is`(expression)
        buildingMethod = null
        ruleKey = null
        return null
    }

    override fun <U> firstOf(vararg methods: U?): U? {
        val expression: ParsingExpression = FirstOfExpression(*pop(methods.size))
        expressionStack.push(expression)
        return null
    }

    override fun <U> optional(method: U): Optional<U>? {
        val expression = pop()
        val grammarRuleKey: GrammarRuleKey = DummyGrammarRuleKey("optional", expression)
        optionals.add(grammarRuleKey)
        b.rule(grammarRuleKey).`is`(b.optional(expression))
        invokeRule(grammarRuleKey)
        return null
    }

    override fun <U> oneOrMore(method: U): List<U>? {
        val expression = pop()
        val grammarRuleKey: GrammarRuleKey = DummyGrammarRuleKey("oneOrMore", expression)
        oneOrMores.add(grammarRuleKey)
        b.rule(grammarRuleKey).`is`(b.oneOrMore(expression))
        invokeRule(grammarRuleKey)
        return null
    }

    override fun <U> zeroOrMore(method: U): Optional<List<U>>? {
        val expression = pop()
        val grammarRuleKey: GrammarRuleKey = DummyGrammarRuleKey("zeroOrMore", expression)
        zeroOrMores.add(grammarRuleKey)
        b.rule(grammarRuleKey).`is`(b.zeroOrMore(expression))
        invokeRule(grammarRuleKey)
        return null
    }

    override fun invokeRule(ruleKey: GrammarRuleKey): AstNode? {
        pushDelayed(ruleKey)
        return null
    }

    override fun token(ruleKey: GrammarRuleKey): T? {
        pushDelayed(ruleKey)
        return null
    }

    private fun pushDelayed(grammarRuleKey: GrammarRuleKey) {
        push(DelayedRuleInvocationExpression(b, grammarRuleKey))
    }

    public fun addAction(method: Method, stackElements: Int) {
        method.isAccessible = true
        val grammarRuleKey: GrammarRuleKey = DummyGrammarRuleKey(method)
        actions[grammarRuleKey] = method
        val expression = if (stackElements == 1) pop() else SequenceExpression(*pop(stackElements))
        b.rule(grammarRuleKey).`is`(expression)
        invokeRule(grammarRuleKey)
    }

    private fun pop(n: Int): Array<ParsingExpression> {
        val result = arrayOfNulls<ParsingExpression>(n)
        for (i in n - 1 downTo 0) {
            result[i] = pop()
        }
        return result.requireNoNulls()
    }

    private fun pop(): ParsingExpression {
        return expressionStack.pop()
    }

    private fun push(expression: ParsingExpression) {
        expressionStack.push(expression)
    }

    public fun actionForRuleKey(ruleKey: Any?): Method? {
        return actions[ruleKey]
    }

    public fun ruleKeyForMethod(method: Method?): GrammarRuleKey? {
        return methodToRuleKey[method]
    }

    public fun hasMethodForRuleKey(ruleKey: Any?): Boolean {
        return mappedRuleKeys.contains(ruleKey)
    }

    public fun isOptionalRule(ruleKey: Any?): Boolean {
        return optionals.contains(ruleKey)
    }

    public fun isOneOrMoreRule(ruleKey: Any?): Boolean {
        return oneOrMores.contains(ruleKey)
    }

    public fun isZeroOrMoreRule(ruleKey: Any?): Boolean {
        return zeroOrMores.contains(ruleKey)
    }

    private class DummyGrammarRuleKey : GrammarRuleKey {
        private val method: Method?
        private val operator: String?
        private val expression: ParsingExpression?

        constructor(method: Method?) {
            this.method = method
            operator = null
            expression = null
        }

        constructor(operator: String?, expression: ParsingExpression?) {
            method = null
            this.operator = operator
            this.expression = expression
        }

        override fun toString(): String {
            if (operator != null) {
                return "$operator($expression)"
            }
            checkNotNull(method) { "the operator or the method should be defined" }
            val sb = StringBuilder()
            sb.append("f.")
            sb.append(method.name)
            sb.append('(')
            val parameterTypes = method.parameterTypes
            for (i in 0 until parameterTypes.size - 1) {
                sb.append(parameterTypes[i].simpleName)
                sb.append(", ")
            }
            if (parameterTypes.isNotEmpty()) {
                sb.append(parameterTypes[parameterTypes.size - 1].simpleName)
            }
            sb.append(')')
            return sb.toString()
        }
    }
}
