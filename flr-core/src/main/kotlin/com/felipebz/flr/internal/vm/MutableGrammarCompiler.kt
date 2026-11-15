//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarRuleKey
import java.util.*

public class MutableGrammarCompiler : CompilationHandler() {
    private val compilationQueue: Queue<CompilableGrammarRule?> = ArrayDeque()
    private val matchers: MutableMap<GrammarRuleKey, CompilableGrammarRule> = HashMap()
    private val offsets: MutableMap<GrammarRuleKey, Int> = HashMap()
    private fun doCompile(start: CompilableGrammarRule): CompiledGrammar {
        val instructions = mutableListOf<Instruction>()

        // Compile
        compilationQueue.add(start)
        matchers[start.ruleKey] = start
        while (compilationQueue.isNotEmpty()) {
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
        val specializedResult = if (usesParserContext) {
            result.map { instruction ->
                when (instruction) {
                    is Instruction.FailTwiceInstruction -> ContextFailTwiceInstruction
                    is Instruction.BackCommitInstruction -> ContextBackCommitInstruction(instruction.offset())
                    else -> instruction
                }
            }.toTypedArray()
        } else {
            result
        }
        val rootRuleOffset = checkNotNull(offsets[start.ruleKey])
        val program = InstructionProgram.link(specializedResult)
        return CompiledGrammar(
            program,
            matchers,
            start.ruleKey,
            rootRuleOffset,
            usesParserContext
        )
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
