//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

/**
 * For unit tests.
 */
class SubExpression(private vararg val ids: Int) : ParsingExpression {
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        val result = arrayOfNulls<Instruction>(
            ids.size
        )
        for (i in ids.indices) {
            result[i] = mockInstruction(ids[i])
        }
        return result.requireNoNulls()
    }

    private class MockInstruction(private val id: Int) : Instruction() {
        override fun toString(): String {
            return "Mock $id"
        }

        override fun execute(machine: Machine) {
            throw UnsupportedOperationException()
        }

        override fun equals(other: Any?): Boolean {
            return other is MockInstruction && id == other.id
        }

        override fun hashCode(): Int {
            return id
        }
    }

    override fun toString(): String {
        return "SubExpression"
    }

    companion object {
        fun mockInstruction(id: Int): Instruction {
            return MockInstruction(id)
        }
    }
}
