//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.typed

import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.Instruction
import com.felipebz.flr.internal.vm.ParsingExpression
import java.lang.reflect.Method

public class DelayedRuleInvocationExpression : ParsingExpression {
    private val b: LexerlessGrammarBuilder
    private val grammarBuilderInterceptor: GrammarBuilderInterceptor<*>?
    private val method: Method?
    private var ruleKey: GrammarRuleKey?

    public constructor(b: LexerlessGrammarBuilder, ruleKey: GrammarRuleKey?) {
        this.b = b
        grammarBuilderInterceptor = null
        method = null
        this.ruleKey = ruleKey
    }

    public constructor(b: LexerlessGrammarBuilder, grammarBuilderInterceptor: GrammarBuilderInterceptor<*>?, method: Method?) {
        this.b = b
        this.grammarBuilderInterceptor = grammarBuilderInterceptor
        this.method = method
        ruleKey = null
    }

    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        if (ruleKey == null) {
            ruleKey = checkNotNull(grammarBuilderInterceptor).ruleKeyForMethod(method)
        }

        val ruleKey = this.ruleKey
        checkNotNull(ruleKey) { "Cannot find the rule key corresponding to the invoked method: " + toString() }
        return try {
            // Ensure the MutableParsingRule is created in the definitions
            b.rule(ruleKey)
            compiler.compile((DEFINITIONS_FIELD[b] as Map<*, *>)[ruleKey] as MutableParsingRule)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    override fun toString(): String {
        return if (ruleKey != null) {
            ruleKey.toString()
        } else {
            checkNotNull(method).name + "()"
        }
    }

    public companion object {
        private val DEFINITIONS_FIELD = ReflectionUtils.getField(LexerlessGrammarBuilder::class.java, "definitions")
    }
}
