//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
