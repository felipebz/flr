//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SequenceExpressionTest {
    @Test
    fun should_compile() {
        val expression = SequenceExpression(
            SubExpression(1, 2),
            SubExpression(3)
        )
        assertThat(expression.toString()).isEqualTo("Sequence[SubExpression, SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        assertThat(instructions).isEqualTo(
            arrayOf(
                SubExpression.mockInstruction(1),
                SubExpression.mockInstruction(2),
                SubExpression.mockInstruction(3)
            )
        )
    }
}
