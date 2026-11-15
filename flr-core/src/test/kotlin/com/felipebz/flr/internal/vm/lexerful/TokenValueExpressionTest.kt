//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
