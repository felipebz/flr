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
package com.felipebz.flr.internal.vm

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.Instruction.*
import com.felipebz.flr.internal.vm.Instruction.Companion.backCommit
import com.felipebz.flr.internal.vm.Instruction.Companion.call
import com.felipebz.flr.internal.vm.Instruction.Companion.choice
import com.felipebz.flr.internal.vm.Instruction.Companion.commit
import com.felipebz.flr.internal.vm.Instruction.Companion.commitVerify
import com.felipebz.flr.internal.vm.Instruction.Companion.jump
import com.felipebz.flr.internal.vm.Instruction.Companion.predicateChoice

class InstructionTest {
    private val machine = mock<Machine>()
    @Test
    fun jump() {
        val instruction = jump(42)
        assertThat(instruction).isInstanceOf(JumpInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("Jump 42")
        assertThat(instruction == jump(42)).isTrue()
        assertThat(instruction == jump(13)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).jump(42)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun call() {
        val matcher = mock<Matcher>()
        val instruction = call(42, matcher)
        assertThat(instruction).isInstanceOf(CallInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("Call 42")
        assertThat(instruction == call(42, matcher)).isTrue()
        assertThat(instruction == call(42, mock())).isFalse()
        assertThat(instruction == call(13, matcher)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).pushReturn(1, matcher, 42)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun choice() {
        val instruction = choice(42)
        assertThat(instruction).isInstanceOf(ChoiceInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("Choice 42")
        assertThat(instruction == choice(42)).isTrue()
        assertThat(instruction == choice(13)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).pushBacktrack(42)
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun predicateChoice() {
        val instruction = predicateChoice(42)
        assertThat(instruction).isInstanceOf(PredicateChoiceInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("PredicateChoice 42")
        assertThat(instruction == predicateChoice(42)).isTrue()
        assertThat(instruction == predicateChoice(13)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).pushBacktrack(42)
        inOrder.verify(machine).ignoreErrors = true
        inOrder.verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun commit() {
        val instruction = commit(42)
        assertThat(instruction).isInstanceOf(CommitInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("Commit " + 42)
        assertThat(instruction == commit(42)).isTrue()
        assertThat(instruction == commit(13)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        val stack = MachineStack().getOrCreateChild()
        whenever(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine, times(2)).peek()
        inOrder.verify(machine).pop()
        inOrder.verify(machine).jump(42)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun commitVerify() {
        val instruction = commitVerify(42)
        assertThat(instruction).isInstanceOf(CommitVerifyInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("CommitVerify " + 42)
        assertThat(instruction == commitVerify(42)).isTrue()
        assertThat(instruction == commitVerify(13)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        val stack = MachineStack().getOrCreateChild()
        whenever(machine.peek()).thenReturn(stack)
        whenever(machine.index).thenReturn(13)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).index
        inOrder.verify(machine, times(3)).peek()
        inOrder.verify(machine).pop()
        inOrder.verify(machine).jump(42)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun commitVerify_should_throw_exception() {
        val instruction = commitVerify(42)
        val stack = MachineStack().getOrCreateChild()
        stack.index = 13
        whenever(machine.peek()).thenReturn(stack)
        whenever(machine.index).thenReturn(13)
        assertThrows<GrammarException>("The inner part of ZeroOrMore and OneOrMore must not allow empty matches") {
            instruction.execute(machine)
        }
    }

    @Test
    fun ret() {
        val instruction = Instruction.ret()
        assertThat(instruction).isInstanceOf(RetInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("Ret")
        assertThat(instruction).`as`("singleton").isSameAs(Instruction.ret())
        val stack = mock<MachineStack>()
        whenever(stack.address).thenReturn(42)
        whenever(stack.ignoreErrors).thenReturn(true)
        whenever(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).createNode()
        inOrder.verify(machine).peek()
        inOrder.verify(machine).ignoreErrors = true
        inOrder.verify(machine).address = 42
        inOrder.verify(machine).popReturn()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun backtrack() {
        val instruction = Instruction.backtrack()
        assertThat(instruction).isInstanceOf(BacktrackInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("Backtrack")
        assertThat(instruction).`as`("singleton").isSameAs(Instruction.backtrack())
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun end() {
        val instruction = Instruction.end()
        assertThat(instruction).isInstanceOf(EndInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("End")
        assertThat(instruction).`as`("singleton").isSameAs(Instruction.end())
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).address = -1
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun failTwice() {
        val instruction = Instruction.failTwice()
        assertThat(instruction).isInstanceOf(FailTwiceInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("FailTwice")
        assertThat(instruction).`as`("singleton").isSameAs(Instruction.failTwice())
        val stack = mock<MachineStack>()
        whenever(stack.index).thenReturn(13)
        whenever(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).peek()
        inOrder.verify(machine).index = 13
        inOrder.verify(machine).pop()
        inOrder.verify(machine).backtrack()
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun backCommit() {
        val instruction = backCommit(42)
        assertThat(instruction).isInstanceOf(BackCommitInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("BackCommit 42")
        assertThat(instruction == backCommit(42)).isTrue()
        assertThat(instruction == backCommit(13)).isFalse()
        assertThat(instruction == Any()).isFalse()
        assertThat(instruction.hashCode()).isEqualTo(42)
        val stack = mock<MachineStack>()
        whenever(stack.index).thenReturn(13)
        whenever(stack.ignoreErrors).thenReturn(true)
        whenever(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = inOrder(machine)
        inOrder.verify(machine).peek()
        inOrder.verify(machine).index = 13
        inOrder.verify(machine).ignoreErrors = true
        inOrder.verify(machine).pop()
        inOrder.verify(machine).jump(42)
        verifyNoMoreInteractions(machine)
    }

    @Test
    fun ignoreErrors() {
        val instruction = Instruction.ignoreErrors()
        assertThat(instruction).isInstanceOf(IgnoreErrorsInstruction::class.java)
        assertThat(instruction.toString()).isEqualTo("IgnoreErrors")
        assertThat(instruction).`as`("singleton").isSameAs(Instruction.ignoreErrors())
        instruction.execute(machine)
        verify(machine).ignoreErrors = true
        verify(machine).jump(1)
        verifyNoMoreInteractions(machine)
    }
}