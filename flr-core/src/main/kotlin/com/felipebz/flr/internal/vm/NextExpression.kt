//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class NextExpression(private val subExpression: ParsingExpression) : ParsingExpression {
    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * Choice L1
     * subExpression
     * BackCommit L2
     * L1: Fail
     * L2: ...
    </pre> *
     *
     * Should be noted that can be compiled without usage of instruction "BackCommit":
     * <pre>
     * Choice L1
     * Choice L2
     * subExpression
     * L2: Commit L3
     * L3: Fail
     * L1: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        val sub = compiler.compile(subExpression)
        val result = arrayOfNulls<Instruction>(
            sub.size + 3
        )
        result[0] = Instruction.choice(result.size - 1)
        sub.copyInto(result, 1)
        result[sub.size + 1] = Instruction.backCommit(2)
        result[sub.size + 2] = Instruction.backtrack()
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "Next[$subExpression]"
    }
}
