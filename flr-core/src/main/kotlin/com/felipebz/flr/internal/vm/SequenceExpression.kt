//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class SequenceExpression(private vararg val subExpressions: ParsingExpression) : ParsingExpression {

    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * subExpressions[0]
     * subExpressions[1]
     * subExpressions[2]
     * ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        val result = mutableListOf<Instruction>()
        for (subExpression in subExpressions) {
            Instruction.addAll(result, compiler.compile(subExpression))
        }
        return result.toTypedArray()
    }

    override fun toString(): String {
        return "Sequence" + subExpressions.contentToString()
    }

}
