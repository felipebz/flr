/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.internal.vm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class StringExpressionTest {
    private val expression = StringExpression("foo")
    private val machine = mock<Machine>()
    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.toString()).isEqualTo("String foo")
    }

    @Test
    fun should_match() {
        whenever(machine.length).thenReturn(3)
        whenever(machine[0]).thenReturn('f')
        whenever(machine[1]).thenReturn('o')
        whenever(machine[2]).thenReturn('o')
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine)[0]
        inOrder.verify(machine)[1]
        inOrder.verify(machine)[2]
        inOrder.verify(machine).createLeafNode(expression, 3)
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
        whenever(machine.length).thenReturn(3)
        whenever(machine[0]).thenReturn('b')
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine)[0]
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }
}