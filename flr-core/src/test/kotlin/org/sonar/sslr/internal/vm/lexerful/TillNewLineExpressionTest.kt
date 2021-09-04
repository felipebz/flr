/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package org.sonar.sslr.internal.vm.lexerful

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import com.sonar.sslr.api.TokenType
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.sonar.sslr.internal.vm.CompilationHandler
import org.sonar.sslr.internal.vm.Machine

class TillNewLineExpressionTest {
    private val expression = TillNewLineExpression.INSTANCE
    private val machine = mock<Machine>()
    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.toString()).isEqualTo("TillNewLine")
    }

    @Test
    fun should_match() {
        val token1 = token(GenericTokenType.IDENTIFIER, 1)
        val token2 = token(GenericTokenType.IDENTIFIER, 1)
        val token3 = token(GenericTokenType.IDENTIFIER, 2)
        whenever(machine.tokenAt(0)).thenReturn(token1)
        whenever(machine.tokenAt(1)).thenReturn(token2)
        whenever(machine.tokenAt(2)).thenReturn(token3)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).tokenAt(1)
        inOrder.verify(machine).tokenAt(2)
        // Number of created nodes must be equal to the number of consumed tokens (2):
        inOrder.verify(machine, times(2)).createLeafNode(expression, 1)
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_match2() {
        val token0 = token(GenericTokenType.IDENTIFIER, 1)
        val token1 = token(GenericTokenType.IDENTIFIER, 1)
        val token2 = token(GenericTokenType.IDENTIFIER, 1)
        val token3 = token(GenericTokenType.EOF, 1)
        whenever(machine.index).thenReturn(1)
        whenever(machine.tokenAt(-1)).thenReturn(token0)
        whenever(machine.tokenAt(0)).thenReturn(token1)
        whenever(machine.tokenAt(1)).thenReturn(token2)
        whenever(machine.tokenAt(2)).thenReturn(token3)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).tokenAt(1)
        inOrder.verify(machine).tokenAt(2)
        // Number of created nodes must be equal to the number of consumed tokens (2):
        inOrder.verify(machine, times(2)).createLeafNode(expression, 1)
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }

    companion object {
        private fun token(tokenType: TokenType, line: Int): Token {
            val token = mock<Token>()
            whenever(token.line).thenReturn(line)
            whenever(token.type).thenReturn(tokenType)
            return token
        }
    }
}