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
package org.sonar.sslr.ast

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeType
import java.util.function.Predicate

/**
 * An immutable ordered collection of AST nodes with operations for selection.
 * Use `[AstNode.select]` to obtain an instance of this interface.
 *
 *
 * This interface is not intended to be implemented by clients.
 *
 * @since 1.18
 */
@Deprecated("in 1.22")
interface AstSelect : Iterable<AstNode?> {
    /**
     * Returns new selection, which contains children of this selection.
     */
    fun children(): AstSelect

    /**
     * Returns new selection, which contains children of a given type of this selection.
     *
     *
     * In the following case, `children("B")` would return "B2" and "B3":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     */
    fun children(type: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains children of a given types of this selection.
     *
     * @see .children
     */
    fun children(vararg types: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains next sibling for each node from this selection.
     *
     *
     * In the following case, for selection "B1" `nextSibling()` would return "B2":
     * <pre>
     * A1
     * |__ B1
     * |    |__ C1
     * |__ B2
    </pre> *
     */
    fun nextSibling(): AstSelect

    /**
     * Returns new selection, which contains previous sibling for each node from this selection.
     *
     *
     * In the following case, for selection "B2" `previousSibling()` would return "B1":
     * <pre>
     * A1
     * |__ B1
     * |    |__ C1
     * |__ B2
    </pre> *
     */
    fun previousSibling(): AstSelect

    /**
     * Returns new selection, which contains parent for each node from this selection.
     */
    fun parent(): AstSelect

    /**
     * Returns new selection, which contains first ancestor of a given type for each node from this selection.
     *
     *
     * In the following case, for selection "B2" `firstAncestor("A")` would return "A2":
     * <pre>
     * A1
     * |__ A2
     * |__ B1
     * |__ B2
    </pre> *
     */
    fun firstAncestor(type: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains first ancestor of one of the given types for each node from this selection.
     *
     * @see .firstAncestor
     */
    fun firstAncestor(vararg types: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains descendants of a given type of this selection.
     * Be careful, this method searches among all descendants whatever is their depth, so favor [.children] when possible.
     *
     *
     * In the following case, `getDescendants("B")` would return "B1", "B2" and "B3":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ D1
     * |__ B3
    </pre> *
     */
    fun descendants(type: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains descendants of a given types of this selection.
     *
     * @see .descendants
     */
    fun descendants(vararg types: AstNodeType): AstSelect

    /**
     * Returns <tt>true</tt> if this selection contains no elements.
     *
     * @return <tt>true</tt> if this selection contains no elements
     */
    fun isEmpty(): Boolean

    /**
     * Returns <tt>true</tt> if this selection contains elements.
     *
     * @return <tt>true</tt> if this selection contains elements
     */
    fun isNotEmpty(): Boolean

    /**
     * Returns new selection, which contains elements of this selection that have given type.
     */
    fun filter(type: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains elements of this selection that have any one of the given types.
     */
    fun filter(vararg types: AstNodeType): AstSelect

    /**
     * Returns new selection, which contains elements of this selection that satisfy a predicate.
     */
    fun filter(predicate: Predicate<AstNode>): AstSelect

    /**
     * Returns the number of elements in this selection.
     *
     * @return the number of elements in this selection
     */
    fun size(): Int

    /**
     * Returns the element at the specified position in this selection.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this selection
     * @throws IndexOutOfBoundsException if the index is out of range
     * (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    operator fun get(index: Int): AstNode

    /**
     * Returns an iterator over the elements in this selection.
     *
     * @return an iterator over the elements in this selection
     */
    override fun iterator(): Iterator<AstNode>
}