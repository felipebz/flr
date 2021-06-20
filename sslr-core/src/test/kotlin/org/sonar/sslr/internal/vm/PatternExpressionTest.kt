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
package org.sonar.sslr.internal.vm

import org.fest.assertions.Assertions
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.vm.Machine

class PatternExpressionTest {
    private val expression = PatternExpression("foo|bar")
    private val machine = Mockito.mock(Machine::class.java)
    @Test
    fun should_compile() {
        Assertions.assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        Assertions.assertThat(expression.toString()).isEqualTo("Pattern foo|bar")
    }

    @Test
    fun should_match() {
        Mockito.`when`(machine.length).thenReturn(3)
        Mockito.`when`(machine[0]).thenReturn('f')
        Mockito.`when`(machine[1]).thenReturn('o')
        Mockito.`when`(machine[2]).thenReturn('o')
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine, Mockito.atLeast(1)).length
        inOrder.verify(machine, Mockito.atLeast(1))[0]
        inOrder.verify(machine, Mockito.atLeast(1))[1]
        inOrder.verify(machine, Mockito.atLeast(1))[2]
        inOrder.verify(machine).createLeafNode(expression, 3)
        inOrder.verify(machine).jump(1)
        Mockito.verifyNoMoreInteractions(machine)

        // Should reset matcher with empty string:
        try {
            expression.getMatcher().find(1)
            Assert.fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            Assertions.assertThat(e.message).isEqualTo("Illegal start index")
        }
    }

    @Test
    fun should_backtrack() {
        Mockito.`when`(machine.length).thenReturn(1)
        Mockito.`when`(machine[0]).thenReturn('z')
        expression.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine, Mockito.atLeast(1)).length
        inOrder.verify(machine, Mockito.atLeast(1))[0]
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)

        // Should reset matcher with empty string:
        try {
            expression.getMatcher().find(1)
            Assert.fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            Assertions.assertThat(e.message).isEqualTo("Illegal start index")
        }
    }

    @Test
    fun should_catch_StackOverflowError() {
        Mockito.`when`(machine.length).thenReturn(1)
        Mockito.`when`(machine[0]).thenThrow(StackOverflowError::class.java)
        assertThrows(
            "The regular expression 'foo|bar' has led to a stack overflow error."
                    + " This error is certainly due to an inefficient use of alternations. See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507",
            GrammarException::class.java
        ) {
            expression.execute(machine)
        }
    }
}