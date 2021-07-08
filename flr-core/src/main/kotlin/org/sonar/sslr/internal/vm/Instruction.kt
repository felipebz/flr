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

import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.matchers.*

public abstract class Instruction {
    /**
     * Executes this instruction.
     */
    public abstract fun execute(machine: Machine)
    public class JumpInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            machine.jump(offset)
        }

        override fun toString(): String {
            return "Jump $offset"
        }

        override fun equals(other: Any?): Boolean {
            return other is JumpInstruction && offset == other.offset
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public class CallInstruction(private val offset: Int, private val matcher: Matcher?) : Instruction() {
        override fun execute(machine: Machine) {
            machine.pushReturn(1, matcher, offset)
        }

        override fun toString(): String {
            return "Call $offset"
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallInstruction) {
                return (offset == other.offset
                        && matcher == other.matcher)
            }
            return false
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public class ChoiceInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            machine.pushBacktrack(offset)
            machine.jump(1)
        }

        override fun toString(): String {
            return "Choice $offset"
        }

        override fun equals(other: Any?): Boolean {
            return other is ChoiceInstruction && offset == other.offset
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public class IgnoreErrorsInstruction : Instruction() {
        override fun execute(machine: Machine) {
            machine.ignoreErrors = true
            machine.jump(1)
        }

        override fun toString(): String {
            return "IgnoreErrors"
        }
    }

    /**
     * Instruction dedicated for predicates.
     * Behaves exactly as [ChoiceInstruction], but disables error reports.
     */
    public class PredicateChoiceInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            machine.pushBacktrack(offset)
            machine.ignoreErrors = true
            machine.jump(1)
        }

        override fun toString(): String {
            return "PredicateChoice $offset"
        }

        override fun equals(other: Any?): Boolean {
            return other is PredicateChoiceInstruction && offset == other.offset
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public class CommitInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            // add all nodes to parent
            machine.peek().parent().subNodes.addAll(machine.peek().subNodes)
            machine.pop()
            machine.jump(offset)
        }

        override fun toString(): String {
            return "Commit $offset"
        }

        override fun equals(other: Any?): Boolean {
            return other is CommitInstruction && offset == other.offset
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public class CommitVerifyInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            if (machine.index == machine.peek().index) {
                // TODO better message, e.g. dump stack
                throw GrammarException("The inner part of ZeroOrMore and OneOrMore must not allow empty matches")
            }
            // add all nodes to parent
            machine.peek().parent().subNodes.addAll(machine.peek().subNodes)
            machine.pop()
            machine.jump(offset)
        }

        override fun toString(): String {
            return "CommitVerify $offset"
        }

        override fun equals(other: Any?): Boolean {
            return other is CommitVerifyInstruction && offset == other.offset
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public class RetInstruction : Instruction() {
        override fun execute(machine: Machine) {
            machine.createNode()
            val stack = machine.peek()
            machine.ignoreErrors = stack.ignoreErrors
            machine.address = stack.address
            machine.popReturn()
        }

        override fun toString(): String {
            return "Ret"
        }
    }

    public class BacktrackInstruction : Instruction() {
        override fun execute(machine: Machine) {
            machine.backtrack()
        }

        override fun toString(): String {
            return "Backtrack"
        }
    }

    public class EndInstruction : Instruction() {
        override fun execute(machine: Machine) {
            machine.address = -1
        }

        override fun toString(): String {
            return "End"
        }
    }

    public class FailTwiceInstruction : Instruction() {
        override fun execute(machine: Machine) {
            // restore state of machine to correctly report error during backtrack
            // note that there is no need restore value of "IgnoreErrors", because this will be done during backtrack
            machine.index = machine.peek().index

            // remove pending alternative pushed by Choice instruction
            machine.pop()
            machine.backtrack()
        }

        override fun toString(): String {
            return "FailTwice"
        }
    }

    public class BackCommitInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            val stack = machine.peek()
            machine.index = stack.index
            machine.ignoreErrors = stack.ignoreErrors
            machine.pop()
            machine.jump(offset)
        }

        override fun toString(): String {
            return "BackCommit $offset"
        }

        override fun equals(other: Any?): Boolean {
            return other is BackCommitInstruction && offset == other.offset
        }

        override fun hashCode(): Int {
            return offset
        }
    }

    public companion object {
        private val RET: Instruction = RetInstruction()
        private val BACKTRACK: Instruction = BacktrackInstruction()
        private val END: Instruction = EndInstruction()
        private val FAIL_TWICE: Instruction = FailTwiceInstruction()
        private val IGNORE_ERRORS: Instruction = IgnoreErrorsInstruction()
        @JvmStatic
        public fun addAll(list: MutableList<Instruction>, array: Array<Instruction>) {
            for (i in array) {
                list.add(i)
            }
        }

        @JvmStatic
        public fun jump(offset: Int): Instruction {
            return JumpInstruction(offset)
        }

        @JvmStatic
        public fun call(offset: Int, matcher: Matcher?): Instruction {
            return CallInstruction(offset, matcher)
        }

        @JvmStatic
        public fun ret(): Instruction {
            return RET
        }

        @JvmStatic
        public fun backtrack(): Instruction {
            return BACKTRACK
        }

        @JvmStatic
        public fun end(): Instruction {
            return END
        }

        @JvmStatic
        public fun choice(offset: Int): Instruction {
            return ChoiceInstruction(offset)
        }

        @JvmStatic
        public fun predicateChoice(offset: Int): Instruction {
            return PredicateChoiceInstruction(offset)
        }

        @JvmStatic
        public fun commit(offset: Int): Instruction {
            return CommitInstruction(offset)
        }

        @JvmStatic
        public fun commitVerify(offset: Int): Instruction {
            return CommitVerifyInstruction(offset)
        }

        @JvmStatic
        public fun failTwice(): Instruction {
            return FAIL_TWICE
        }

        @JvmStatic
        public fun backCommit(offset: Int): Instruction {
            return BackCommitInstruction(offset)
        }

        @JvmStatic
        public fun ignoreErrors(): Instruction {
            return IGNORE_ERRORS
        }
    }
}