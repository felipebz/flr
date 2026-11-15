//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
