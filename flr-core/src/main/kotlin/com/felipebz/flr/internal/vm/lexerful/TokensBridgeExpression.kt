//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm.lexerful

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.NativeExpression

public class TokensBridgeExpression(private val from: TokenType, private val to: TokenType) : NativeExpression(), Matcher {
    override fun execute(machine: Machine) {
        val length = machine.length
        if (length < 2 || machine.tokenAt(0).type !== from) {
            machine.backtrack()
            return
        }
        var offset = 0
        var bridgeLevel = 1
        var token: Token?
        do {
            offset++
            if (offset >= length) {
                machine.backtrack()
                return
            }
            token = machine.tokenAt(offset)
            when {
                token.type === from -> {
                    bridgeLevel++
                }
                token.type === to -> {
                    bridgeLevel--
                }
                else -> {
                    // nop
                }
            }
        } while (bridgeLevel != 0)
        for (i in 0..offset) {
            machine.createLeafNode(this, 1)
        }
        machine.jump(1)
    }

    override fun toString(): String {
        return "Bridge[$from,$to]"
    }
}
