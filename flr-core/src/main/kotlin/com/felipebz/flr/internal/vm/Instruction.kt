//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.internal.matchers.Matcher

public abstract class Instruction {
    /**
     * Executes this instruction.
     */
    public abstract fun execute(machine: Machine)

    public data class JumpInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            machine.jump(offset)
        }

        override fun toString(): String {
            return "Jump $offset"
        }
    }

    public data class CallInstruction(
        internal val offset: Int,
        internal val matcher: Matcher?,
        public val targetId: Int
    ) : Instruction() {
        override fun execute(machine: Machine) {
            machine.pushReturn(1, matcher, offset, targetId)
        }

        override fun toString(): String {
            return "Call $offset"
        }
    }

    internal data class UnlinkedCallInstruction(val offset: Int, val matcher: Matcher?) : Instruction() {
        override fun execute(machine: Machine): Unit = error("Call instruction must be linked before execution")

        override fun toString(): String = "Call $offset"
    }

    public data class ChoiceInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            machine.pushBacktrack(offset)
            machine.jump(1)
        }

        override fun toString(): String {
            return "Choice $offset"
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
    public data class PredicateChoiceInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            machine.pushBacktrack(offset)
            machine.ignoreErrors = true
            machine.jump(1)
        }

        override fun toString(): String {
            return "PredicateChoice $offset"
        }
    }

    public data class CommitInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            // add all nodes to parent
            machine.peek().parent().subNodes.addAll(machine.peek().subNodes)
            machine.pop()
            machine.jump(offset)
        }

        override fun toString(): String {
            return "Commit $offset"
        }
    }

    public data class CommitVerifyInstruction(private val offset: Int) : Instruction() {
        override fun execute(machine: Machine) {
            if (machine.index == machine.peek().index) {
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

    public data class BackCommitInstruction(private val offset: Int) : Instruction() {
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

        internal fun offset(): Int {
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
            return UnlinkedCallInstruction(offset, matcher)
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
