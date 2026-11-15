//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.TokenType
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.NativeExpression

public class TokenTypesExpression(vararg types: TokenType) : NativeExpression(), Matcher {
    private val types: MutableSet<TokenType> = types.toHashSet()

    override fun execute(machine: Machine) {
        if (machine.isEmpty() || !types.contains(machine.tokenAt(0).type)) {
            machine.backtrack()
            return
        }
        machine.createLeafNode(this, 1)
        machine.jump(1)
    }

    override fun toString(): String {
        return "TokenTypes $types"
    }
}
