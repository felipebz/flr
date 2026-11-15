//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.vm.Instruction.Companion.choice
import com.felipebz.flr.internal.vm.Instruction.Companion.commit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FirstOfExpressionTest {
    @Test
    fun should_compile() {
        val expression = FirstOfExpression(
            SubExpression(1, 2, 3),
            SubExpression(4, 5),
            SubExpression(6)
        )
        assertThat(expression.toString()).isEqualTo("FirstOf[SubExpression, SubExpression, SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        assertThat(instructions).isEqualTo(
            arrayOf(
                choice(5),
                SubExpression.mockInstruction(1),
                SubExpression.mockInstruction(2),
                SubExpression.mockInstruction(3),
                commit(6),
                choice(4),
                SubExpression.mockInstruction(4),
                SubExpression.mockInstruction(5),
                commit(2),
                SubExpression.mockInstruction(6)
            )
        )
    }
}
