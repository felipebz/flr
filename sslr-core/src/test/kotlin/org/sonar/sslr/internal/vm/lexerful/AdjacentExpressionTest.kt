/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.sslr.internal.vm.lexerful

import com.sonar.sslr.api.Token
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.internal.vm.CompilationHandler
import org.sonar.sslr.internal.vm.Machine

class AdjacentExpressionTest {
    private val expression = AdjacentExpression.INSTANCE
    private val machine = Mockito.mock(Machine::class.java)
    @Test
    fun should_compile() {
        Assertions.assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        Assertions.assertThat(expression.toString()).isEqualTo("Adjacent")
    }

    @Test
    fun should_match() {
        val previousToken = Mockito.mock(Token::class.java)
        Mockito.`when`(previousToken.value).thenReturn("foo")
        Mockito.`when`(previousToken.line).thenReturn(42)
        Mockito.`when`(previousToken.column).thenReturn(13)
        val nextToken = Mockito.mock(Token::class.java)
        Mockito.`when`(nextToken.line).thenReturn(42)
        Mockito.`when`(nextToken.column).thenReturn(13 + 3)
        Mockito.`when`(machine.getIndex()).thenReturn(1)
        Mockito.`when`(machine.tokenAt(-1)).thenReturn(previousToken)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(nextToken)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).getIndex()
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).jump(1)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack() {
        val previousToken = Mockito.mock(Token::class.java)
        Mockito.`when`(previousToken.value).thenReturn("foo")
        Mockito.`when`(previousToken.line).thenReturn(42)
        Mockito.`when`(previousToken.column).thenReturn(13)
        val nextToken = Mockito.mock(Token::class.java)
        Mockito.`when`(nextToken.line).thenReturn(42 + 1)
        Mockito.`when`(nextToken.column).thenReturn(13 + 3)
        Mockito.`when`(machine.getIndex()).thenReturn(1)
        Mockito.`when`(machine.tokenAt(-1)).thenReturn(previousToken)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(nextToken)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).getIndex()
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack2() {
        val previousToken = Mockito.mock(Token::class.java)
        Mockito.`when`(previousToken.value).thenReturn("foo")
        Mockito.`when`(previousToken.line).thenReturn(42)
        Mockito.`when`(previousToken.column).thenReturn(13)
        val nextToken = Mockito.mock(Token::class.java)
        Mockito.`when`(nextToken.line).thenReturn(13)
        Mockito.`when`(nextToken.column).thenReturn(42)
        Mockito.`when`(machine.getIndex()).thenReturn(1)
        Mockito.`when`(machine.tokenAt(-1)).thenReturn(previousToken)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(nextToken)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).getIndex()
        inOrder.verify(machine).tokenAt(-1)
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack3() {
        Mockito.`when`(machine.getIndex()).thenReturn(0)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).getIndex()
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }
}