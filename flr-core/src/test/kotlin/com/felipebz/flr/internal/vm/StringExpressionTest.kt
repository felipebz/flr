//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
