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
package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.Machine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class TokensBridgeExpressionTest {
    private val fromType = mock<TokenType>()
    private val toType = mock<TokenType>()
    private val anotherType = mock<TokenType>()
    private val expression = TokensBridgeExpression(fromType, toType)
    private val machine = mock<Machine>()
    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.toString()).isEqualTo("Bridge[$fromType,$toType]")
    }

    @Test
    fun should_match() {
        whenever(machine.length).thenReturn(5)
        val token1 = token(fromType)
        val token2 = token(fromType)
        val token3 = token(anotherType)
        val token4 = token(toType)
        val token5 = token(toType)
        whenever(machine.tokenAt(0)).thenReturn(token1)
        whenever(machine.tokenAt(1)).thenReturn(token2)
        whenever(machine.tokenAt(2)).thenReturn(token3)
        whenever(machine.tokenAt(3)).thenReturn(token4)
        whenever(machine.tokenAt(4)).thenReturn(token5)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).tokenAt(1)
        inOrder.verify(machine).tokenAt(2)
        inOrder.verify(machine).tokenAt(3)
        inOrder.verify(machine).tokenAt(4)
        // Number of created nodes must be equal to the number of consumed tokens (5):
        inOrder.verify(machine, times(5)).createLeafNode(expression, 1)
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack() {
        whenever(machine.length).thenReturn(0)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack2() {
        whenever(machine.length).thenReturn(2)
        val token1 = token(anotherType)
        whenever(machine.tokenAt(0)).thenReturn(token1)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack3() {
        whenever(machine.length).thenReturn(2)
        val token1 = token(fromType)
        val token2 = token(fromType)
        whenever(machine.tokenAt(0)).thenReturn(token1)
        whenever(machine.tokenAt(1)).thenReturn(token2)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).tokenAt(1)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    companion object {
        private fun token(tokenType: TokenType): Token {
            val token = mock<Token>()
            whenever(token.type).thenReturn(tokenType)
            return token
        }
    }
}
