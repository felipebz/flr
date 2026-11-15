//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class NextNotExpression(private val subExpression: ParsingExpression) : ParsingExpression {
    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * Choice L1
     * subExpression
     * FailTwice
     * L1: ...
    </pre> *
     *
     * Should be noted that can be compiled without usage of instruction "FailTwice":
     * <pre>
     * Choice L2
     * subExpression
     * Commit L1
     * L1: Fail
     * L2: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        val sub = compiler.compile(subExpression)
        val result = arrayOfNulls<Instruction>(
            sub.size + 2
        )
        result[0] = Instruction.predicateChoice(sub.size + 2)
        sub.copyInto(result, 1)
        result[sub.size + 1] = Instruction.failTwice()
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "NextNot[$subExpression]"
    }
}
