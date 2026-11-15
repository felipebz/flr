//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.vm.Instruction.Companion.backtrack
import com.felipebz.flr.internal.vm.Instruction.Companion.choice
import com.felipebz.flr.internal.vm.Instruction.Companion.commitVerify
import com.felipebz.flr.internal.vm.Instruction.Companion.jump
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
