/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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

        override fun equals(obj: Any?): Boolean {
            return obj is MockInstruction && id == obj.id
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
