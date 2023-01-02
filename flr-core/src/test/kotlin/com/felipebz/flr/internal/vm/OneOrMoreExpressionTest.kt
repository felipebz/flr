/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.felipebz.flr.internal.vm.Instruction.Companion.backtrack
import com.felipebz.flr.internal.vm.Instruction.Companion.choice
import com.felipebz.flr.internal.vm.Instruction.Companion.commitVerify
import com.felipebz.flr.internal.vm.Instruction.Companion.jump

class OneOrMoreExpressionTest {
    @Test
    fun should_compile() {
        val expression = OneOrMoreExpression(SubExpression(1, 2))
        assertThat(expression.toString()).isEqualTo("OneOrMore[SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        assertThat(instructions).isEqualTo(
            arrayOf(
                choice(6),
                SubExpression.mockInstruction(1),
                SubExpression.mockInstruction(2),
                commitVerify(1),
                choice(3),
                jump(-4),
                backtrack()
            )
        )
    }
}