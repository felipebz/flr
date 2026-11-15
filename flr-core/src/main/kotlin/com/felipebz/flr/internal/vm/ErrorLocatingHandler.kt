//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public class ErrorLocatingHandler : MachineHandler {
    private var errorIndex = -1
    override fun onBacktrack(machine: Machine) {
        if (errorIndex < machine.index) {
            errorIndex = machine.index
        }
    }

    public fun getErrorIndex(): Int {
        return errorIndex
    }
}
