/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.internal.vm

import com.sonar.sslr.api.GenericTokenType
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.internal.vm.Instruction.Companion.call
import org.sonar.sslr.internal.vm.Instruction.Companion.ignoreErrors
import org.sonar.sslr.internal.vm.Instruction.Companion.jump
import org.sonar.sslr.internal.vm.Instruction.Companion.ret

class TokenExpressionTest {
    @Test
    fun should_compile() {
        val expression = TokenExpression(GenericTokenType.IDENTIFIER, SubExpression(1, 2))
        Assertions.assertThat(expression.toString()).isEqualTo("Token IDENTIFIER[SubExpression]")
        val instructions = expression.compile(CompilationHandler())
        Assertions.assertThat(instructions).isEqualTo(
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
        val expression = TokenExpression(
            GenericTokenType.IDENTIFIER, Mockito.mock(
                ParsingExpression::class.java
            )
        )
        // Important for AstCreator
        Assertions.assertThat(expression.getTokenType()).isSameAs(GenericTokenType.IDENTIFIER)
    }
}