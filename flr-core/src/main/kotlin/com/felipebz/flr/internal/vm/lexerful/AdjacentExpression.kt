//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.NativeExpression

public object AdjacentExpression : NativeExpression() {
    override fun execute(machine: Machine) {
        if (machine.index == 0) {
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
}
