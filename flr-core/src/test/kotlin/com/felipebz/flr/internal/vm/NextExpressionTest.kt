//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.vm.Instruction.Companion.backCommit
import com.felipebz.flr.internal.vm.Instruction.Companion.backtrack
import com.felipebz.flr.internal.vm.Instruction.Companion.choice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NextExpressionTest {
    @Test
    fun should_compile() {
        val expression = NextExpression(SubExpression(1, 2))
        assertThat(expression.toString()).isEqualTo("Next[SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        assertThat(instructions).isEqualTo(
            arrayOf(
                choice(4),
                SubExpression.mockInstruction(1),
                SubExpression.mockInstruction(2),
                backCommit(2),
                backtrack()
            )
        )
    }
}
