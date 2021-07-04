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
package org.sonar.sslr.internal.vm.lexerful

import org.sonar.sslr.internal.vm.Machine
import org.sonar.sslr.internal.vm.NativeExpression

class AdjacentExpression private constructor() : NativeExpression() {
    override fun execute(machine: Machine) {
        if (machine.getIndex() == 0) {
            machine.backtrack()
            return
        }
        val previousToken = machine.tokenAt(-1)
        val nextToken = machine.tokenAt(0)
        if (nextToken.column <= previousToken.column + previousToken.value.length
            && nextToken.line == previousToken.line
        ) {
            machine.jump(1)
        } else {
            machine.backtrack()
        }
    }

    override fun toString(): String {
        return "Adjacent"
    }

    companion object {
        @JvmField
        val INSTANCE = AdjacentExpression()
    }
}