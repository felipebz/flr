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

import com.felipebz.flr.internal.vm.Instruction.Companion.failTwice
import com.felipebz.flr.internal.vm.Instruction.Companion.predicateChoice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NextNotExpressionTest {
    @Test
    fun should_compile() {
        val expression = NextNotExpression(SubExpression(1, 2))
        assertThat(expression.toString()).isEqualTo("NextNot[SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        assertThat(instructions).isEqualTo(
            arrayOf(
                predicateChoice(4),
                SubExpression.mockInstruction(1),
                SubExpression.mockInstruction(2),
                failTwice()
            )
        )
    }
}
