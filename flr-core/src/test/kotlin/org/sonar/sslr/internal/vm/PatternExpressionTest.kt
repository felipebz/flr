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
package org.sonar.sslr.internal.vm

import org.fest.assertions.Assertions.assertThat
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.*
import org.sonar.sslr.grammar.GrammarException

class PatternExpressionTest {
    private val expression = PatternExpression("foo|bar")
    private val machine = mock<Machine>()
    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.toString()).isEqualTo("Pattern foo|bar")
    }

    @Test
    fun should_match() {
        whenever(machine.length).thenReturn(3)
        whenever(machine[0]).thenReturn('f')
        whenever(machine[1]).thenReturn('o')
        whenever(machine[2]).thenReturn('o')
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine, atLeast(1)).length
        inOrder.verify(machine, atLeast(1))[0]
        inOrder.verify(machine, atLeast(1))[1]
        inOrder.verify(machine, atLeast(1))[2]
        inOrder.verify(machine).createLeafNode(expression, 3)
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)

        // Should reset matcher with empty string:
        try {
            expression.getMatcher().find(1)
            Assert.fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            assertThat(e.message).isEqualTo("Illegal start index")
        }
    }

    @Test
    fun should_backtrack() {
        whenever(machine.length).thenReturn(1)
        whenever(machine[0]).thenReturn('z')
        expression.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine, atLeast(1)).length
        inOrder.verify(machine, atLeast(1))[0]
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)

        // Should reset matcher with empty string:
        try {
            expression.getMatcher().find(1)
            Assert.fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            assertThat(e.message).isEqualTo("Illegal start index")
        }
    }

    @Test
    fun should_catch_StackOverflowError() {
        whenever(machine.length).thenReturn(1)
        whenever(machine[0]).thenThrow(StackOverflowError::class.java)
        assertThrows(
            "The regular expression 'foo|bar' has led to a stack overflow error."
                    + " This error is certainly due to an inefficient use of alternations. See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507",
            GrammarException::class.java
        ) {
            expression.execute(machine)
        }
    }
}