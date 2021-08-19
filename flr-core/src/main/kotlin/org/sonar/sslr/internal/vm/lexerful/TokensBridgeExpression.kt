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
package org.sonar.sslr.internal.vm.lexerful

import com.sonar.sslr.api.Token
import com.sonar.sslr.api.TokenType
import org.sonar.sslr.internal.matchers.Matcher
import org.sonar.sslr.internal.vm.Machine
import org.sonar.sslr.internal.vm.NativeExpression

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