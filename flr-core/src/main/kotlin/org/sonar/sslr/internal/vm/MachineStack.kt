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

public class MachineStack {
    private var child: MachineStack? = null
    private val parent: MachineStack?
    public val subNodes: MutableList<ParseNode>
    public var address: Int = 0
    public var index: Int = 0
    public var ignoreErrors: Boolean = false
    public var matcher: Matcher? = null
    public var leftRecursion: Int = 0
    public var calledAddress: Int = 0

    public constructor() {
        parent = null
        subNodes = mutableListOf()
        index = -1
    }

    private constructor(parent: MachineStack) {
        this.parent = parent
        subNodes = mutableListOf()
    }

    public fun parent(): MachineStack {
        return checkNotNull(parent)
    }

    public fun getOrCreateChild(): MachineStack {
        child = child ?: MachineStack(this)
        return child as MachineStack
    }

    public fun isReturn(): Boolean {
        return matcher != null
    }

    /**
     * @return true, if this object denotes an empty stack
     */
    public fun isEmpty(): Boolean {
        return index == -1
    }
}