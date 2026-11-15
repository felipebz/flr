//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public object NothingExpression : NativeExpression() {
    override fun execute(machine: Machine) {
        machine.backtrack()
    }

    override fun toString(): String {
        return "Nothing"
    }
}
