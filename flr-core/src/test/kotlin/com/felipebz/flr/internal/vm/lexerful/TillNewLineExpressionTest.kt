//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.internal.vm.CompilationHandler
import com.felipebz.flr.internal.vm.Machine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class TillNewLineExpressionTest {
    private val expression = TillNewLineExpression
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
