/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2026 Felipe Zorzo
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

import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.internal.matchers.Matcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MachineTest {
    @Test
    fun subSequence_not_supported() {
        val machine = Machine("", emptyArray())
        assertThrows<UnsupportedOperationException> {
            machine.subSequence(0, 0)
        }
    }

    @Test
    fun test_initial_state() {
        val machine = Machine("", arrayOf(mock(), mock()))
        assertThat(machine.address).isEqualTo(0)
        assertThat(machine.index).isEqualTo(0)
        assertThat(machine.peek().isEmpty()).isTrue()
    }

    @Test
    fun should_jump() {
        val machine = Machine("", arrayOf(mock(), mock()))
        assertThat(machine.address).isEqualTo(0)
        machine.jump(42)
        assertThat(machine.address).isEqualTo(42)
        machine.jump(13)
        assertThat(machine.address).isEqualTo(42 + 13)
    }

    @Test
    fun should_advanceIndex() {
        val machine = Machine("foo bar", arrayOf(mock(), mock()))
        assertThat(machine.index).isEqualTo(0)
        assertThat(machine.length).isEqualTo(7)
        assertThat(machine[0]).isEqualTo('f')
        assertThat(machine[1]).isEqualTo('o')
        machine.advanceIndex(3)
        assertThat(machine.index).isEqualTo(3)
        assertThat(machine.length).isEqualTo(4)
        assertThat(machine[0]).isEqualTo(' ')
        assertThat(machine[1]).isEqualTo('b')
        machine.advanceIndex(1)
        assertThat(machine.index).isEqualTo(4)
        assertThat(machine.length).isEqualTo(3)
        assertThat(machine[0]).isEqualTo('b')
        assertThat(machine[1]).isEqualTo('a')
    }

    @Test
    fun should_pushReturn() {
        val machine = Machine("foo", arrayOf(mock(), mock(), mock()))
        val matcher = mock<Matcher>()
        machine.advanceIndex(1)
        machine.jump(1)
        val previousStack = machine.peek()
        machine.pushReturn(2, matcher, 1)
        assertThat(machine.address).`as`("new address").isEqualTo(2)
        assertThat(machine.peek()).isNotSameAs(previousStack)
        assertThat(machine.peek().parent()).isSameAs(previousStack)
        assertThat(machine.peek().index).`as`("current index").isEqualTo(1)
        assertThat(machine.peek().address).`as`("return address").isEqualTo(1 + 2)
        assertThat(machine.peek().matcher).isSameAs(matcher)
    }

    @Test
    fun should_detect_left_recursion() {
        val machine = Machine("foo", arrayOf(mock(), mock()))
        val matcher = mock<Matcher>()
        machine.advanceIndex(1)
        machine.pushReturn(0, matcher, 1)
        assertThat(machine.peek().calledAddress).isEqualTo(1)
        assertThat(machine.peek().leftRecursion).isEqualTo(-1)

        // same rule, but another index of input sequence
        machine.advanceIndex(1)
        machine.pushReturn(0, matcher, 0)
        assertThat(machine.peek().calledAddress).isEqualTo(1)
        assertThat(machine.peek().leftRecursion).isEqualTo(1)

        // same rule and index of input sequence
        assertThrows<GrammarException>("Left recursion has been detected, involved rule: $matcher") {
            machine.pushReturn(0, matcher, 0)
        }
    }

    @Test
    fun should_pushBacktrack() {
        val machine = Machine("foo", arrayOf(mock(), mock()))
        machine.advanceIndex(1)
        machine.jump(42)
        val previousStack = machine.peek()
        machine.pushBacktrack(13)
        assertThat(machine.peek()).isNotSameAs(previousStack)
        assertThat(machine.peek().parent()).isSameAs(previousStack)
        assertThat(machine.peek().index).`as`("current index").isEqualTo(1)
        assertThat(machine.peek().address).`as`("backtrack address").isEqualTo(42 + 13)
        assertThat(machine.peek().matcher).isNull()
    }

    @Test
    fun should_pop() {
        val machine = Machine("", arrayOf(mock(), mock()))
        val previousStack = machine.peek()
        machine.pushBacktrack(13)
        assertThat(machine.peek()).isNotSameAs(previousStack)
        machine.pop()
        assertThat(machine.peek()).isSameAs(previousStack)
    }

    @Test
    fun should_fail() {
        val machine = Machine("", arrayOf(mock(), mock(), mock()))
        val matcher = mock<Matcher>()
        machine.pushReturn(13, matcher, 0)
        machine.pushReturn(13, matcher, 1)
        machine.backtrack()
        assertThat(machine.address).isEqualTo(-1)
        // TODO matched=false
    }

    @Test
    fun should_backtrack() {
        val machine = Machine("", arrayOf(mock(), mock(), mock(), mock()))
        val matcher = mock<Matcher>()
        val previousStack = machine.peek()
        machine.pushBacktrack(42)
        machine.pushReturn(13, matcher, 0)
        machine.pushReturn(13, matcher, 1)
        machine.backtrack()
        assertThat(machine.peek()).isSameAs(previousStack)
        assertThat(machine.address).isEqualTo(42)
    }

    @Test
    fun should_createLeafNode() {
        val machine = Machine("", arrayOf(mock(), mock()))
        val matcher = mock<Matcher>()
        machine.advanceIndex(42)
        machine.createLeafNode(matcher, 13)
        val node = machine.peek().subNodes[0]
        assertThat(node.matcher).isSameAs(matcher)
        assertThat(node.startIndex).isEqualTo(42)
        assertThat(node.endIndex).isEqualTo(42 + 13)
        assertThat(node.children).isEmpty()
    }

    @Test
    fun should_createNode() {
        val machine = Machine(" ", arrayOf(mock(), mock()))
        val matcher = mock<Matcher>()
        machine.advanceIndex(1)
        // remember startIndex and matcher
        machine.pushReturn(0, matcher, 0)
        val subMatcher = mock<Matcher>()
        machine.createLeafNode(subMatcher, 2)
        machine.createLeafNode(subMatcher, 3)
        machine.createNode()
        val node = machine.peek().parent().subNodes[0]
        assertThat(node.matcher).isSameAs(matcher)
        assertThat(node.startIndex).isEqualTo(1)
        assertThat(node.endIndex).isEqualTo(1 + 2 + 3)
        assertThat(node.children).hasSize(2)
    }

    @Test
    fun should_use_memo() {
        val machine = Machine("foo", arrayOf(mock(), mock(), mock()))
        val matcher = mock<MemoParsingExpression>()
        whenever(matcher.shouldMemoize()).thenReturn(true)
        machine.pushBacktrack(0)
        machine.pushReturn(1, matcher, 2)
        machine.advanceIndex(3)
        machine.createNode()
        val memo = machine.peek().parent().subNodes[0]
        machine.backtrack()
        machine.pushReturn(2, matcher, 1)
        assertThat(machine.address).isEqualTo(2)
        assertThat(machine.index).isEqualTo(3)
        assertThat(machine.peek().subNodes).containsOnly(memo)
    }

    @Test
    fun should_not_memorize() {
        val machine = Machine("foo", arrayOf(mock(), mock(), mock()))
        val matcher = mock<MemoParsingExpression>()
        whenever(matcher.shouldMemoize()).thenReturn(false)
        machine.pushBacktrack(0)
        machine.pushReturn(1, matcher, 2)
        machine.advanceIndex(3)
        machine.createNode()
        machine.backtrack()
        machine.pushReturn(2, matcher, 1)
        assertThat(machine.address).isEqualTo(1)
        assertThat(machine.index).isEqualTo(0)
        assertThat(machine.peek().subNodes).isEmpty()
    }

    @Test
    fun should_not_use_memo() {
        val machine = Machine("foo", arrayOf(mock(), mock(), mock()))
        val matcher = mock<MemoParsingExpression>()
        whenever(matcher.shouldMemoize()).thenReturn(true)
        machine.pushBacktrack(0)
        machine.pushReturn(2, matcher, 1)
        machine.advanceIndex(3)
        machine.createNode()
        machine.backtrack()
        val anotherMatcher = mock<Matcher>()
        machine.pushReturn(2, anotherMatcher, 1)
        assertThat(machine.address).isEqualTo(1)
        assertThat(machine.index).isEqualTo(0)
        assertThat(machine.peek().subNodes).isEmpty()
    }

    @Test
    fun should_clear_stale_subnodes_when_reusing_stack_frame() {
        val machine = Machine("foo", arrayOf(mock(), mock(), mock()))
        val matcher = mock<Matcher>()
        machine.pushReturn(0, matcher, 0)
        machine.createLeafNode(mock(), 1)
        assertThat(machine.peek().subNodes).hasSize(1)
        machine.popReturn()
        assertThat(machine.peek().isEmpty()).isTrue()

        // Reusing the same child frame at depth 1
        machine.pushReturn(0, matcher, 0)
        assertThat(machine.peek().subNodes).isEmpty()
    }

    @Test
    fun should_not_leak_stale_children_when_backtrack_frame_reuses_return_frame() {
        val machine = Machine("foobar", arrayOf(mock(), mock(), mock(), mock()))
        val matcher = mock<Matcher>()
        // Frame 1 as return frame with child
        machine.pushReturn(0, matcher, 0)
        machine.createLeafNode(mock(), 1)
        assertThat(machine.peek().subNodes).hasSize(1)
        machine.popReturn()

        // Frame 1 reused as backtrack frame
        machine.pushBacktrack(10)
        assertThat(machine.peek().matcher).isNull()
        // If child matches and commits, it should only have its own child, not the stale one
        val leafMatcher = mock<Matcher>()
        machine.createLeafNode(leafMatcher, 2)
        Instruction.CommitInstruction(0).execute(machine)
        // Root frame should now have only the new leaf node
        assertThat(machine.peek().subNodes).hasSize(1)
        assertThat(machine.peek().subNodes[0].matcher).isSameAs(leafMatcher)
    }

    @Test
    fun should_start_with_empty_subnodes_when_return_frame_reuses_backtrack_frame() {
        val machine = Machine("foobar", Array(10) { mock() })
        // Frame 1 as backtrack frame that failed and backtracked
        machine.pushBacktrack(1)
        machine.createLeafNode(mock(), 1)
        machine.backtrack()

        // Frame 1 reused as return frame
        val matcher = mock<Matcher>()
        machine.pushReturn(0, matcher, 0)
        assertThat(machine.peek().matcher).isSameAs(matcher)
        assertThat(machine.peek().subNodes).isEmpty()
        val leafMatcher = mock<Matcher>()
        machine.createLeafNode(leafMatcher, 2)
        machine.createNode()
        assertThat(machine.peek().parent().subNodes[0].children).hasSize(1)
        assertThat(machine.peek().parent().subNodes[0].children[0].matcher).isSameAs(leafMatcher)
    }

    @Test
    fun should_handle_nested_choice_and_rule_frame_reuse() {
        val machine = Machine("foobar", Array(20) { mock() })
        val rule1 = mock<Matcher>()
        val rule2 = mock<Matcher>()

        // Depth 1: Return frame
        machine.pushReturn(0, rule1, 1)
        // Depth 2: Backtrack frame
        machine.pushBacktrack(1)
        // Depth 3: Return frame with leaf
        machine.pushReturn(0, rule2, 2)
        machine.createLeafNode(mock(), 1)
        machine.createNode()
        machine.popReturn()
        // Commit depth 2
        Instruction.CommitInstruction(0).execute(machine)
        machine.createNode()
        machine.popReturn()

        // Now reuse frames with roles swapped, advancing index to avoid left recursion
        machine.advanceIndex(1)
        // Depth 1: Backtrack frame
        machine.pushBacktrack(1)
        // Depth 2: Return frame
        machine.pushReturn(0, rule2, 3)
        assertThat(machine.peek().subNodes).isEmpty()
        machine.createLeafNode(mock(), 2)
        machine.createNode()
        machine.popReturn()
        // Commit depth 1
        Instruction.CommitInstruction(0).execute(machine)
        assertThat(machine.peek().subNodes).hasSize(2)
    }
}
