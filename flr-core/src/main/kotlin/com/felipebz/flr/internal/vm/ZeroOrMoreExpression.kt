//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class ZeroOrMoreExpression(private val subExpression: ParsingExpression) : ParsingExpression {
    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * L1: Choice L2
     * subExpression
     * CommitVerify L1
     * L2: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        // TODO maybe can be optimized by introduction of new instruction PartialCommit
        val sub = compiler.compile(subExpression)
        val result = arrayOfNulls<Instruction>(
            sub.size + 2
        )
        result[0] = Instruction.choice(sub.size + 2)
        sub.copyInto(result, 1)
        result[sub.size + 1] = Instruction.commitVerify(-1 - sub.size)
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "ZeroOrMore[$subExpression]"
    }
}
