/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2026 Felipe Zorzo
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
