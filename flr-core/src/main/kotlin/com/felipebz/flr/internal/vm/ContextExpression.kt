//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.ContextKey

internal class ContextExpression(
    private val key: ContextKey<*>,
    private val value: Any?,
    private val present: Boolean,
    private val subExpression: ParsingExpression
) : ParsingExpression {
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        compiler.markParserContextUsed()
        val sub = compiler.compile(subExpression)
        val result = arrayOfNulls<Instruction>(sub.size + 2)
        result[0] = ContextEnterInstruction(key, value, present)
        sub.copyInto(result, 1)
        result[sub.size + 1] = ContextExitInstruction
        return result.requireNoNulls()
    }

    override fun toString(): String {
        return if (present) {
            "WithContext[$key=$value, $subExpression]"
        } else {
            "WithoutContext[$key, $subExpression]"
        }
    }
}

internal class ContextPredicateExpression(
    private val key: ContextKey<*>,
    private val expected: Any?,
    private val requirePresent: Boolean
) : NativeExpression() {
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        compiler.markParserContextUsed()
        return super.compile(compiler)
    }

    override fun execute(machine: Machine) {
        val matches = if (requirePresent) {
            machine.containsContext(key)
        } else {
            machine.matchesContext(key, expected)
        }
        if (matches) {
            machine.jump(1)
        } else {
            machine.backtrack()
        }
    }

    override fun toString(): String {
        return if (requirePresent) {
            "RequireContext[$key]"
        } else {
            "RequireContext[$key=$expected]"
        }
    }
}

internal class ContextEnterInstruction(
    private val key: ContextKey<*>,
    private val value: Any?,
    private val present: Boolean
) : Instruction() {
    override fun execute(machine: Machine) {
        machine.enterContext(key, value, present)
        machine.jump(1)
    }

    override fun toString(): String {
        return if (present) {
            "ContextEnter $key=$value"
        } else {
            "ContextEnter $key=<absent>"
        }
    }
}

internal object ContextExitInstruction : Instruction() {
    override fun execute(machine: Machine) {
        machine.exitContext()
        machine.jump(1)
    }

    override fun toString(): String {
        return "ContextExit"
    }
}

internal object ContextFailTwiceInstruction : Instruction() {
    override fun execute(machine: Machine) {
        machine.index = machine.peek().index
        machine.restoreContextFromCheckpoint()
        machine.pop()
        machine.backtrack()
    }

    override fun toString(): String {
        return "ContextFailTwice"
    }
}

internal class ContextBackCommitInstruction(private val offset: Int) : Instruction() {
    override fun execute(machine: Machine) {
        val stack = machine.peek()
        machine.index = stack.index
        machine.ignoreErrors = stack.ignoreErrors
        machine.restoreContextFromCheckpoint()
        machine.pop()
        machine.jump(offset)
    }

    override fun toString(): String {
        return "ContextBackCommit $offset"
    }
}
