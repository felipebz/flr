//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class FirstOfExpression(private vararg val subExpressions: ParsingExpression) : ParsingExpression {

    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * Choice L1
     * subExpression[0]
     * Commit E
     * L1: Choice L2
     * subExpression[1]
     * Commit E
     * L2: Choice L3
     * subExpression[2]
     * Commit E
     * L3: subExpression[3]
     * E: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        var index = 0
        val sub = subExpressions.map {
            val compiledExpression = compiler.compile(it)
            index += compiledExpression.size
            compiledExpression
        }

        val result = arrayOfNulls<Instruction>(index + (subExpressions.size - 1) * 2)
        index = 0
        for (i in 0 until subExpressions.size - 1) {
            result[index] = Instruction.choice(sub[i].size + 2)
            sub[i].copyInto(result, index + 1, 0)
            index += sub[i].size + 1
            result[index] = Instruction.commit(result.size - index)
            index++
        }
        sub[sub.size - 1].copyInto(result, index, 0)
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "FirstOf" + subExpressions.contentToString()
    }

}
