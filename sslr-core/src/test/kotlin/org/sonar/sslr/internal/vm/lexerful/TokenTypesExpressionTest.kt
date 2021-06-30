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

class TokenTypesExpressionTest {
    private val type1 = Mockito.mock(TokenType::class.java)
    private val type2 = Mockito.mock(TokenType::class.java)
    private val expression = TokenTypesExpression(type1, type2)
    private val machine = Mockito.mock(Machine::class.java)
    @Test
    fun should_compile() {
        Assertions.assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        Assertions.assertThat(expression.toString()).startsWith("TokenTypes [Mock for TokenType, ")
    }

    @Test
    fun should_match() {
        val token = Mockito.mock(Token::class.java)
        Mockito.`when`(token.type).thenReturn(type1)
        Mockito.`when`(machine.length).thenReturn(1)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(token)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).createLeafNode(expression, 1)
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
        val token = Mockito.mock(Token::class.java)
        Mockito.`when`(token.type).thenReturn(
            Mockito.mock(
                TokenType::class.java
            )
        )
        Mockito.`when`(machine.length).thenReturn(1)
        Mockito.`when`(machine.tokenAt(0)).thenReturn(token)
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).length
        inOrder.verify(machine).tokenAt(0)
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }
}