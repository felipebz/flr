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
