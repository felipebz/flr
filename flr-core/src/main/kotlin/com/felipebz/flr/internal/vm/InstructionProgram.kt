//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.matchers.Matcher

/** Instructions and dense call-target metadata linked once, after final layout. */
public class InstructionProgram private constructor(
    public val instructions: Array<Instruction>,
    public val callTargetCount: Int
) {
    public companion object {
        @JvmStatic
        public fun link(instructions: Array<Instruction>): InstructionProgram {
            val targetIds = HashMap<Int, Int>()
            val linked = Array(instructions.size) { address ->
                val instruction = instructions[address]
                val offset: Int
                val matcher: Matcher?
                when (instruction) {
                    is Instruction.UnlinkedCallInstruction -> {
                        offset = instruction.offset
                        matcher = instruction.matcher
                    }
                    is Instruction.CallInstruction -> {
                        offset = instruction.offset
                        matcher = instruction.matcher
                    }
                    else -> return@Array instruction
                }
                val targetAddress = address + offset
                val targetId = targetIds.getOrPut(targetAddress) { targetIds.size }
                Instruction.CallInstruction(offset, matcher, targetId)
            }
            return InstructionProgram(linked, targetIds.size)
        }
    }
}
