//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
