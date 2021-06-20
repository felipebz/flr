/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.sslr.internal.matchers

/**
 * Node of a parse tree.
 */
class ParseNode {
    private val startIndex: Int
    private val endIndex: Int
    private val children: List<ParseNode>
    private val matcher: Matcher?

    constructor(startIndex: Int, endIndex: Int, children: List<ParseNode>, matcher: Matcher?) {
        this.startIndex = startIndex
        this.endIndex = endIndex
        this.children = ArrayList(children)
        this.matcher = matcher
    }

    /**
     * Leaf node.
     */
    constructor(startIndex: Int, endIndex: Int, matcher: Matcher?) {
        this.startIndex = startIndex
        this.endIndex = endIndex
        this.matcher = matcher
        children = emptyList()
    }

    fun getStartIndex(): Int {
        return startIndex
    }

    /**
     * Be aware that element of input with this index is not included into this node.
     */
    fun getEndIndex(): Int {
        return endIndex
    }

    fun getChildren(): List<ParseNode> {
        return children
    }

    fun getMatcher(): Matcher? {
        return matcher
    }
}