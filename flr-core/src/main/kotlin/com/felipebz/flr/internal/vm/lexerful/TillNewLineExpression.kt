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

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.NativeExpression

public object TillNewLineExpression : NativeExpression(), Matcher {
    override fun execute(machine: Machine) {
        val currentLine = if (machine.index == 0) 1 else machine.tokenAt(-1).line
        var offset = 0
        var token = machine.tokenAt(offset)
        while (token.line == currentLine && token.type !== GenericTokenType.EOF) {
            offset++
            token = machine.tokenAt(offset)
        }
        for (i in 0 until offset) {
            machine.createLeafNode(this, 1)
        }
        machine.jump(1)
    }

    override fun toString(): String {
        return "TillNewLine"
    }
}