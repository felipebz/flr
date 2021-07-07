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