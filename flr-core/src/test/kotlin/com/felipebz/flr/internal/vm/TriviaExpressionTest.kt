//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.Trivia.TriviaKind
import com.felipebz.flr.internal.vm.Instruction.Companion.call
import com.felipebz.flr.internal.vm.Instruction.Companion.ignoreErrors
import com.felipebz.flr.internal.vm.Instruction.Companion.jump
import com.felipebz.flr.internal.vm.Instruction.Companion.ret
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class TriviaExpressionTest {
    @Test
    fun should_compile() {
        val expression = TriviaExpression(TriviaKind.COMMENT, SubExpression(1, 2))
        assertThat(expression.toString()).isEqualTo("Trivia COMMENT[SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        assertThat(instructions).isEqualTo(
            arrayOf(
                call(2, expression),
                jump(5),
                ignoreErrors(),
                SubExpression.mockInstruction(1),
                SubExpression.mockInstruction(2),
                ret()
            )
        )
    }

    @Test
    fun should_implement_Matcher() {
        val expression = TriviaExpression(TriviaKind.COMMENT, mock())
        // Important for AstCreator
        assertThat(expression.getTriviaKind()).isSameAs(TriviaKind.COMMENT)
    }
}
