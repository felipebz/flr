/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
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
import com.sonar.sslr.api.TokenType
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.internal.vm.CompilationHandler
import org.sonar.sslr.internal.vm.Machine

class TokensBridgeExpressionTest {
    private val fromType = Mockito.mock(TokenType::class.java)
    private val toType = Mockito.mock(TokenType::class.java)
    private val anotherType = Mockito.mock(TokenType::class.java)
    private val expression = TokensBridgeExpression(fromType, toType)
    private val machine = Mockito.mock(Machine::class.java)
    @Test
    fun should_compile() {
        Assertions.assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        Assertions.assertThat(expression.toString()).isEqualTo("Bridge[$fromType,$toType]")
    }

    @Test
    fun should_match() {
        Mockito.`when`(machine.length).thenReturn(5)
        val token1 = token(fromType)
        val token2 = token(fromType)
        val token3 = token(anotherType)
        val token4 = token(toType)
        val token5 = token(toType)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(token1)
        Mockito.`when`(machine.tokenAt(1)).thenReturn(token2)
        Mockito.`when`(machine.tokenAt(2)).thenReturn(token3)
        Mockito.`when`(machine.tokenAt(3)).thenReturn(token4)
        Mockito.`when`(machine.tokenAt(4)).thenReturn(token5)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).tokenAt(1)
        inOrder.verify(machine).tokenAt(2)
        inOrder.verify(machine).tokenAt(3)
        inOrder.verify(machine).tokenAt(4)
        // Number of created nodes must be equal to the number of consumed tokens (5):
        inOrder.verify(machine, Mockito.times(5)).createLeafNode(expression, 1)
        inOrder.verify(machine).jump(1)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack() {
        Mockito.`when`(machine.length).thenReturn(0)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack2() {
        Mockito.`when`(machine.length).thenReturn(2)
        val token1 = token(anotherType)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(token1)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun should_backtrack3() {
        Mockito.`when`(machine.length).thenReturn(2)
        val token1 = token(fromType)
        val token2 = token(fromType)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(token1)
        Mockito.`when`(machine.tokenAt(1)).thenReturn(token2)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).tokenAt(1)
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    companion object {
        private fun token(tokenType: TokenType): Token {
            val token = Mockito.mock(Token::class.java)
            Mockito.`when`(token.type).thenReturn(tokenType)
            return token
        }
    }
}