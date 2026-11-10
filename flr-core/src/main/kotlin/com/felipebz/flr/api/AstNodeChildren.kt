/**
 * FLR
 * Copyright (C) 2021-2026 Felipe Zorzo
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
package com.felipebz.flr.api

/*
 * Read-only storage of the children of an AstNode. The tree builders only create [SingleChild] and [ChildArray], so
 * the code that reads the children of a parsed tree sees at most two list classes. [GrowableChildArray] is used only
 * when AstNode.addChild appends to a node that already has children.
 */

/** The child of a node with exactly one child, without an array. */
internal class SingleChild(private val child: AstNode) : AbstractList<AstNode>(), RandomAccess {
    override val size: Int
        get() = 1

    override fun get(index: Int): AstNode {
        if (index != 0) {
            throw IndexOutOfBoundsException("Index: $index, Size: 1")
        }
        return child
    }
}

/** The children of a node in an array of the exact size; the shared [EMPTY] instance stands for no children. */
internal class ChildArray(private val elements: Array<AstNode>) : AbstractList<AstNode>(), RandomAccess {
    override val size: Int
        get() = elements.size

    override fun get(index: Int): AstNode = elements[index]

    companion object {
        @JvmField
        val EMPTY: ChildArray = ChildArray(emptyArray())
    }
}

/** The children of a node that [AstNode.addChild] grows, with spare capacity at the end of the array. */
internal class GrowableChildArray(first: List<AstNode>, next: AstNode) : AbstractList<AstNode>(), RandomAccess {
    private var elements = arrayOfNulls<AstNode>(maxOf(MIN_CAPACITY, (first.size + 1) * 2))
    override var size: Int = 0
        private set

    init {
        for (i in first.indices) {
            elements[i] = first[i]
        }
        elements[first.size] = next
        size = first.size + 1
    }

    override fun get(index: Int): AstNode {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        return checkNotNull(elements[index])
    }

    fun add(child: AstNode) {
        if (size == elements.size) {
            elements = elements.copyOf(size * 2)
        }
        elements[size++] = child
    }

    private companion object {
        const val MIN_CAPACITY = 4
    }
}
