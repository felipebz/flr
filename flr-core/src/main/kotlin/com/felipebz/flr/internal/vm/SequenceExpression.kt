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
