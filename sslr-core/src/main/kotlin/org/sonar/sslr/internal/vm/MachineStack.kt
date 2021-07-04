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
package org.sonar.sslr.internal.vm

import org.sonar.sslr.internal.matchers.Matcher
import org.sonar.sslr.internal.matchers.ParseNode

class MachineStack {
    private var child: MachineStack? = null
    private val parent: MachineStack?
    val subNodes: MutableList<ParseNode>
    var address = 0
    var index = 0
    var ignoreErrors = false
    var matcher: Matcher? = null
    var leftRecursion = 0
    var calledAddress = 0

    constructor() {
        parent = null
        subNodes = mutableListOf()
        index = -1
    }

    private constructor(parent: MachineStack) {
        this.parent = parent
        subNodes = mutableListOf()
    }

    fun parent(): MachineStack {
        return checkNotNull(parent)
    }

    fun getOrCreateChild(): MachineStack {
        child = child ?: MachineStack(this)
        return child as MachineStack
    }

    fun isReturn(): Boolean {
        return matcher != null
    }

    /**
     * @return true, if this object denotes an empty stack
     */
    fun isEmpty(): Boolean {
        return index == -1
    }
}