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
