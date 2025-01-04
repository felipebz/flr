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
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.Machine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class AdjacentExpressionTest {
    private val expression = AdjacentExpression
    private val machine = mock<Machine>()
    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.toString()).isEqualTo("Adjacent")
    }

    @Test
    fun should_match() {
        val previousToken = mock<Token>()
        whenever(previousToken.value).thenReturn("foo")
        whenever(previousToken.line).thenReturn(42)
        whenever(previousToken.column).thenReturn(13)
        val nextToken = mock<Token>()
        whenever(nextToken.line).thenReturn(42)
        whenever(nextToken.column).thenReturn(13 + 3)
        whenever(machine.index).thenReturn(1)
        whenever(machine.tokenAt(-1)).thenReturn(previousToken)
        whenever(machine.tokenAt(0)).thenReturn(nextToken)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack() {
        val previousToken = mock<Token>()
        whenever(previousToken.value).thenReturn("foo")
        whenever(previousToken.line).thenReturn(42)
        whenever(previousToken.column).thenReturn(13)
        val nextToken = mock<Token>()
        whenever(nextToken.line).thenReturn(42 + 1)
        whenever(nextToken.column).thenReturn(13 + 3)
        whenever(machine.index).thenReturn(1)
        whenever(machine.tokenAt(-1)).thenReturn(previousToken)
        whenever(machine.tokenAt(0)).thenReturn(nextToken)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack2() {
        val previousToken = mock<Token>()
        whenever(previousToken.value).thenReturn("foo")
        whenever(previousToken.line).thenReturn(42)
        whenever(previousToken.column).thenReturn(13)
        val nextToken = mock<Token>()
        whenever(nextToken.line).thenReturn(13)
        whenever(nextToken.column).thenReturn(42)
        whenever(machine.index).thenReturn(1)
        whenever(machine.tokenAt(-1)).thenReturn(previousToken)
        whenever(machine.tokenAt(0)).thenReturn(nextToken)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack3() {
        whenever(machine.index).thenReturn(0)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }
}
