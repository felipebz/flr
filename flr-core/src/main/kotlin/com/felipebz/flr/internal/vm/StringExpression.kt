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
package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.matchers.Matcher

public class StringExpression(private val string: String) : NativeExpression(), Matcher {
    override fun execute(machine: Machine) {
        if (machine.length < string.length) {
            machine.backtrack()
            return
        }
        for (i in string.indices) {
            if (machine[i] != string[i]) {
                machine.backtrack()
                return
            }
        }
        machine.createLeafNode(this, string.length)
        machine.jump(1)
    }

    override fun toString(): String {
        return "String $string"
    }
}