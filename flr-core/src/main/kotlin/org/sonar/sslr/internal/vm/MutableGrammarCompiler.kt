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
package org.sonar.sslr.internal.vm

import org.sonar.sslr.grammar.GrammarRuleKey
import java.util.*

public class MutableGrammarCompiler : CompilationHandler() {
    private val compilationQueue: Queue<CompilableGrammarRule?> = ArrayDeque()
    private val matchers: MutableMap<GrammarRuleKey, CompilableGrammarRule> = HashMap()
    private val offsets: MutableMap<GrammarRuleKey, Int> = HashMap()
    private fun doCompile(start: CompilableGrammarRule): CompiledGrammar {
        val instructions: MutableList<Instruction> = ArrayList()

        // Compile
        compilationQueue.add(start)
        matchers[start.ruleKey] = start
        while (!compilationQueue.isEmpty()) {
            val rule = compilationQueue.poll()
            val ruleKey = checkNotNull(rule).ruleKey
            offsets[ruleKey] = instructions.size
            Instruction.addAll(instructions, compile(checkNotNull(rule.expression)))
            instructions.add(Instruction.ret())
        }

        // Link
        val result: Array<Instruction> = instructions.toTypedArray()
        for (i in result.indices) {
            val instruction = result[i]
            if (instruction is RuleRefExpression) {
                val ruleKey = instruction.getRuleKey()
                val offset = checkNotNull(offsets[ruleKey])
                result[i] = Instruction.call(offset - i, matchers[ruleKey])
            }
        }
        return CompiledGrammar(result, matchers, start.ruleKey, checkNotNull(offsets[start.ruleKey]))
    }

    override fun compile(expression: ParsingExpression): Array<Instruction> {
        return if (expression is CompilableGrammarRule) {
            if (!matchers.containsKey(expression.ruleKey)) {
                compilationQueue.add(expression)
                matchers[expression.ruleKey] = expression
            }
            expression.compile(this)
        } else {
            expression.compile(this)
        }
    }

    public companion object {
        public fun compile(rule: CompilableGrammarRule): CompiledGrammar {
            return MutableGrammarCompiler().doCompile(rule)
        }
    }
}