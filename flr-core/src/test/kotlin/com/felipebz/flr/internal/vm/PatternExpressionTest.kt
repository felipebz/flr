//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.mockito.kotlin.*

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
            expression.matcher.find(1)
            fail("exception expected")
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
            expression.matcher.find(1)
            fail("exception expected")
        } catch (e: IndexOutOfBoundsException) {
            assertThat(e.message).isEqualTo("Illegal start index")
        }
    }

    @Test
    fun should_catch_StackOverflowError() {
        whenever(machine.length).thenReturn(1)
        whenever(machine[0]).thenThrow(StackOverflowError::class.java)
        assertThrows<GrammarException> (
            "The regular expression 'foo|bar' has led to a stack overflow error." +
                    " This error is certainly due to an inefficient use of alternations. See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507") {
            expression.execute(machine)
        }
    }
}
