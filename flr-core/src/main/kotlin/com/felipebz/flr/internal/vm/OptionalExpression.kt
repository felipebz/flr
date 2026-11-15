//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class OptionalExpression(private val subExpression: ParsingExpression) : ParsingExpression {
    /**
     * Compiles this expression into a sequence of instructions:
     *
     * Choice L1
     * subExpression
     * Commit L1
     * L1: ...
     *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        // not described in paper
        val instr = compiler.compile(subExpression)
        val result = arrayOfNulls<Instruction>(
            instr.size + 2
        )
        result[0] = Instruction.choice(result.size)
        instr.copyInto(result, 1)
        result[instr.size + 1] = Instruction.commit(1)
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "Optional[$subExpression]"
    }
}
