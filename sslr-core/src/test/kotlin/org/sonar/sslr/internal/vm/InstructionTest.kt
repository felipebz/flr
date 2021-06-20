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
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.matchers.Matcher
import org.sonar.sslr.internal.vm.Instruction.*
import org.sonar.sslr.internal.vm.Instruction.Companion.backCommit
import org.sonar.sslr.internal.vm.Instruction.Companion.call
import org.sonar.sslr.internal.vm.Instruction.Companion.choice
import org.sonar.sslr.internal.vm.Instruction.Companion.commit
import org.sonar.sslr.internal.vm.Instruction.Companion.commitVerify
import org.sonar.sslr.internal.vm.Instruction.Companion.jump
import org.sonar.sslr.internal.vm.Instruction.Companion.predicateChoice
import org.sonar.sslr.internal.vm.Machine

class InstructionTest {
    private val machine = Mockito.mock(Machine::class.java)
    @Test
    fun jump() {
        val instruction = jump(42)
        Assertions.assertThat(instruction).isInstanceOf(JumpInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("Jump 42")
        Assertions.assertThat(instruction == jump(42)).isTrue()
        Assertions.assertThat(instruction == jump(13)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).jump(42)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun call() {
        val matcher = Mockito.mock(
            Matcher::class.java
        )
        val instruction = call(42, matcher)
        Assertions.assertThat(instruction).isInstanceOf(CallInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("Call 42")
        Assertions.assertThat(instruction == call(42, matcher)).isTrue()
        Assertions.assertThat(
            instruction == call(
                42, Mockito.mock(
                    Matcher::class.java
                )
            )
        ).isFalse()
        Assertions.assertThat(instruction == call(13, matcher)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).pushReturn(1, matcher, 42)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun choice() {
        val instruction = choice(42)
        Assertions.assertThat(instruction).isInstanceOf(ChoiceInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("Choice 42")
        Assertions.assertThat(instruction == choice(42)).isTrue()
        Assertions.assertThat(instruction == choice(13)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).pushBacktrack(42)
        inOrder.verify(machine).jump(1)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun predicateChoice() {
        val instruction = predicateChoice(42)
        Assertions.assertThat(instruction).isInstanceOf(PredicateChoiceInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("PredicateChoice 42")
        Assertions.assertThat(instruction == predicateChoice(42)).isTrue()
        Assertions.assertThat(instruction == predicateChoice(13)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).pushBacktrack(42)
        inOrder.verify(machine).setIgnoreErrors(true)
        inOrder.verify(machine).jump(1)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun commit() {
        val instruction = commit(42)
        Assertions.assertThat(instruction).isInstanceOf(CommitInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("Commit " + 42)
        Assertions.assertThat(instruction == commit(42)).isTrue()
        Assertions.assertThat(instruction == commit(13)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        val stack = MachineStack().getOrCreateChild()
        Mockito.`when`(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine, Mockito.times(2)).peek()
        inOrder.verify(machine).pop()
        inOrder.verify(machine).jump(42)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun commitVerify() {
        val instruction = commitVerify(42)
        Assertions.assertThat(instruction).isInstanceOf(CommitVerifyInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("CommitVerify " + 42)
        Assertions.assertThat(instruction == commitVerify(42)).isTrue()
        Assertions.assertThat(instruction == commitVerify(13)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        val stack = MachineStack().getOrCreateChild()
        Mockito.`when`(machine.peek()).thenReturn(stack)
        Mockito.`when`(machine.getIndex()).thenReturn(13)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).getIndex()
        inOrder.verify(machine, Mockito.times(3)).peek()
        inOrder.verify(machine).pop()
        inOrder.verify(machine).jump(42)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun commitVerify_should_throw_exception() {
        val instruction = commitVerify(42)
        val stack = MachineStack().getOrCreateChild()
        stack.setIndex(13)
        Mockito.`when`(machine.peek()).thenReturn(stack)
        Mockito.`when`(machine.getIndex()).thenReturn(13)
        assertThrows("The inner part of ZeroOrMore and OneOrMore must not allow empty matches", GrammarException::class.java) {
            instruction.execute(machine)
        }
    }

    @Test
    fun ret() {
        val instruction = Instruction.ret()
        Assertions.assertThat(instruction).isInstanceOf(RetInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("Ret")
        Assertions.assertThat(instruction).`as`("singleton").isSameAs(Instruction.ret())
        val stack = Mockito.mock(MachineStack::class.java)
        Mockito.`when`(stack.address()).thenReturn(42)
        Mockito.`when`(stack.isIgnoreErrors()).thenReturn(true)
        Mockito.`when`(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).createNode()
        inOrder.verify(machine).peek()
        inOrder.verify(machine).setIgnoreErrors(true)
        inOrder.verify(machine).setAddress(42)
        inOrder.verify(machine).popReturn()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun backtrack() {
        val instruction = Instruction.backtrack()
        Assertions.assertThat(instruction).isInstanceOf(BacktrackInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("Backtrack")
        Assertions.assertThat(instruction).`as`("singleton").isSameAs(Instruction.backtrack())
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun end() {
        val instruction = Instruction.end()
        Assertions.assertThat(instruction).isInstanceOf(EndInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("End")
        Assertions.assertThat(instruction).`as`("singleton").isSameAs(Instruction.end())
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).setAddress(-1)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun failTwice() {
        val instruction = Instruction.failTwice()
        Assertions.assertThat(instruction).isInstanceOf(FailTwiceInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("FailTwice")
        Assertions.assertThat(instruction).`as`("singleton").isSameAs(Instruction.failTwice())
        val stack = Mockito.mock(MachineStack::class.java)
        Mockito.`when`(stack.index()).thenReturn(13)
        Mockito.`when`(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).peek()
        inOrder.verify(machine).setIndex(13)
        inOrder.verify(machine).pop()
        inOrder.verify(machine).backtrack()
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun backCommit() {
        val instruction = backCommit(42)
        Assertions.assertThat(instruction).isInstanceOf(BackCommitInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("BackCommit 42")
        Assertions.assertThat(instruction == backCommit(42)).isTrue()
        Assertions.assertThat(instruction == backCommit(13)).isFalse()
        Assertions.assertThat(instruction == Any()).isFalse()
        Assertions.assertThat(instruction.hashCode()).isEqualTo(42)
        val stack = Mockito.mock(MachineStack::class.java)
        Mockito.`when`(stack.index()).thenReturn(13)
        Mockito.`when`(stack.isIgnoreErrors()).thenReturn(true)
        Mockito.`when`(machine.peek()).thenReturn(stack)
        instruction.execute(machine)
        val inOrder = Mockito.inOrder(machine)
        inOrder.verify(machine).peek()
        inOrder.verify(machine).setIndex(13)
        inOrder.verify(machine).setIgnoreErrors(true)
        inOrder.verify(machine).pop()
        inOrder.verify(machine).jump(42)
        Mockito.verifyNoMoreInteractions(machine)
    }

    @Test
    fun ignoreErrors() {
        val instruction = Instruction.ignoreErrors()
        Assertions.assertThat(instruction).isInstanceOf(IgnoreErrorsInstruction::class.java)
        Assertions.assertThat(instruction.toString()).isEqualTo("IgnoreErrors")
        Assertions.assertThat(instruction).`as`("singleton").isSameAs(Instruction.ignoreErrors())
        instruction.execute(machine)
        Mockito.verify(machine).setIgnoreErrors(true)
        Mockito.verify(machine).jump(1)
        Mockito.verifyNoMoreInteractions(machine)
    }
}