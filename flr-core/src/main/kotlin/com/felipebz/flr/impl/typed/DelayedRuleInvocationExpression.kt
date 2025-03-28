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
