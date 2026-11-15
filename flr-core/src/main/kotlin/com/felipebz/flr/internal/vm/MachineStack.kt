//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.matchers.ParseNode

public class MachineStack {
    private var child: MachineStack? = null
    private val parent: MachineStack?
    public val subNodes: MutableList<ParseNode>
    public var address: Int = 0
    public var index: Int = 0
    public var ignoreErrors: Boolean = false
    public var matcher: Matcher? = null
    public var previousCallState: Int = 0
    public var calledTargetId: Int = -1
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
