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
package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.Token
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.Machine

class TokenValueExpressionTest {
    private val expression = TokenValueExpression("foo")
    private val machine = mock<Machine>()
    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.toString()).isEqualTo("TokenValue foo")
    }

    @Test
    fun should_match() {
        val token = mock<Token>()
        whenever(token.value).thenReturn("foo")
        whenever(machine.length).thenReturn(1)
        whenever(machine.tokenAt(0)).thenReturn(token)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).createLeafNode(expression, 1)
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
        val token = mock<Token>()
        whenever(token.value).thenReturn("bar")
        whenever(machine.length).thenReturn(1)
        whenever(machine.tokenAt(0)).thenReturn(token)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack3() {
        val token = mock<Token>()
        whenever(token.value).thenReturn("h31" /* same hash code as for "foo" */)
        whenever(machine.length).thenReturn(1)
        whenever(machine.tokenAt(0)).thenReturn(token)
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }
}