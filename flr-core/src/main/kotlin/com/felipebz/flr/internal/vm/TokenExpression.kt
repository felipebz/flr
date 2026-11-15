//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
