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

public class OneOrMoreExpression(private val subExpression: ParsingExpression) : ParsingExpression {
    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * Choice L3
     * L1: subExpression
     * CommitVerify L2
     * L2: Choice L4
     * Jump L1
     * L3: Fail
     * L4: ....
    </pre> *
     *
     * Should be noted that can be compiled with help of [ZeroOrMoreExpression]:
     * <pre>
     * subExpresson
     * L1: Choice L2
     * subExpression
     * CommitVerify L1
     * L2: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        val sub = compiler.compile(subExpression)
        val result = arrayOfNulls<Instruction>(
            sub.size + 5
        )
        result[0] = Instruction.choice(sub.size + 4)
        System.arraycopy(sub, 0, result, 1, sub.size)
        result[sub.size + 1] = Instruction.commitVerify(1)
        result[sub.size + 2] = Instruction.choice(3)
        result[sub.size + 3] = Instruction.jump(-2 - sub.size)
        result[sub.size + 4] = Instruction.backtrack()
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "OneOrMore[$subExpression]"
    }
}