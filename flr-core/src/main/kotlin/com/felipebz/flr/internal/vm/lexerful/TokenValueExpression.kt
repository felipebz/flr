/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.NativeExpression

public class TokenValueExpression(private val value: String) : NativeExpression(), Matcher {
    override fun execute(machine: Machine) {
        if (machine.isEmpty()) {
            machine.backtrack()
            return
        }
        val token = machine.tokenAt(0)
        if (value.hashCode() == token.value.hashCode() && value == token.value) {
            machine.createLeafNode(this, 1)
            machine.jump(1)
        } else {
            machine.backtrack()
        }
    }

    override fun toString(): String {
        return "TokenValue $value"
    }
}