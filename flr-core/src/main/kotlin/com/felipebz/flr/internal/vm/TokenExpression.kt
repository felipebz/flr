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

import com.felipebz.flr.api.TokenType
import com.felipebz.flr.internal.matchers.Matcher

public class TokenExpression(private val tokenType: TokenType, private val subExpression: ParsingExpression) : Matcher,
    ParsingExpression {
    public fun getTokenType(): TokenType {
        return tokenType
    }

    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * Call L1
     * Jump L2
     * L1: subExpression
     * Return
     * L2: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        return compile(compiler, this, subExpression)
    }

    override fun toString(): String {
        return "Token $tokenType[$subExpression]"
    }

    public companion object {
        /**
         * Helper method to reduce duplication between [TokenExpression] and [TriviaExpression].
         */
        public fun compile(
            compiler: CompilationHandler,
            expression: Matcher,
            subExpression: ParsingExpression
        ): Array<Instruction> {
            // TODO maybe can be optimized
            val instr = compiler.compile(subExpression)
            val result = arrayOfNulls<Instruction>(
                instr.size + 4
            )
            result[0] = Instruction.call(2, expression)
            result[1] = Instruction.jump(instr.size + 3)
            result[2] = Instruction.ignoreErrors()
            instr.copyInto(result, 3)
            result[3 + instr.size] = Instruction.ret()
            return result.requireNoNulls()
        }
    }
}
