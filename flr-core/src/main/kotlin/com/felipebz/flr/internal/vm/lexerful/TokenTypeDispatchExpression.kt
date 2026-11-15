//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.TokenType
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.Instruction
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.NativeExpression
import com.felipebz.flr.internal.vm.ParsingExpression

/**
 * Selects one compiled expression from the current token type without creating
 * ordered-choice frames for alternatives that cannot match.
 *
 * Kept internal because token-type dispatch is currently used as a specialized
 * grammar optimization rather than as part of the public grammar-builder API.
 */
public class TokenTypeDispatchExpression(
    private val branches: Map<TokenType, ParsingExpression>
) : NativeExpression() {

    init {
        require(branches.isNotEmpty())
    }

    override fun execute(machine: Machine) {
        throw UnsupportedOperationException()
    }

    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        val compiledBranches = branches.entries.map { (tokenType, expression) ->
            tokenType to compiler.compile(expression)
        }
        val size = 1 + compiledBranches.sumOf { it.second.size + 1 }
        val result = arrayOfNulls<Instruction>(size)
        val offsets = HashMap<TokenType, Int>(branches.size)
        result[0] = DispatchInstruction(offsets)

        var index = 1
        for ((tokenType, instructions) in compiledBranches) {
            offsets[tokenType] = index
            instructions.copyInto(result, index)
            index += instructions.size
            result[index] = Instruction.jump(size - index)
            index++
        }

        return result.requireNoNulls()
    }

    override fun toString(): String {
        return "TokenTypeDispatch ${branches.keys}"
    }

    private class DispatchInstruction(
        private val offsets: Map<TokenType, Int>
    ) : Instruction() {
        override fun execute(machine: Machine) {
            if (machine.isEmpty()) {
                machine.backtrack()
                return
            }

            val tokenType = machine.tokenAt(0).type
            val offset = offsets[tokenType]
            if (offset == null) {
                machine.backtrack()
            } else {
                machine.jump(offset)
            }
        }

        override fun toString(): String {
            return "TokenTypeDispatchInstruction $offsets"
        }
    }
}
