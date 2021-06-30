/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import com.sonar.sslr.api.GenericTokenType
import org.sonar.sslr.internal.matchers.Matcher
import org.sonar.sslr.internal.vm.Machine
import org.sonar.sslr.internal.vm.NativeExpression

class TillNewLineExpression private constructor() : NativeExpression(), Matcher {
    override fun execute(machine: Machine) {
        val currentLine = if (machine.getIndex() == 0) 1 else machine.tokenAt(-1).line
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

    companion object {
        @JvmField
        val INSTANCE = TillNewLineExpression()
    }
}