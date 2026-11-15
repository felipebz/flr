//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
