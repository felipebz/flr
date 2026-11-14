/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
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

import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.grammar.MutableParsingRule

/**
 * the parser is in charge to construct an abstract syntax tree (AST) which is a tree representation of the abstract syntactic structure of
 * source code. Each node of the tree is an AstNode and each node denotes a construct occurring in the source code which starts at a given
 * Token.
 *
 * @see Token
 */
public open class AstNode(
    type: AstNodeType, public val name: String,
    /**
     * Get the Token associated to this AstNode
     */
    public val tokenOrNull: Token?
) {
    public val token: Token
        get() = checkNotNull(tokenOrNull)

    public var type: AstNodeType = type
        private set

    /**
     * Children of this node: a shared empty list, a single child, or an array of the exact size. Only [addChild] on a
     * node that already has children leaves spare capacity, so that adding children one by one stays cheap.
     */
    private var childStorage: List<AstNode> = ChildArray.EMPTY

    /**
     * Get the list of children. The list is read-only; use [addChild] to add a child.
     *
     * @return list of children
     */
    public val children: List<AstNode>
        get() = childStorage
    private var childIndex = -1

    /**
     * Get the parent of this node in the tree.
     */
    public var parentOrNull: AstNode? = null
        private set
    public val parent: AstNode get() = checkNotNull(parentOrNull)

    public constructor(token: Token) : this(token.type, token.type.name, token)

    public fun addChild(child: AstNode?) {
        if (child != null) {
            if (child.hasToBeSkippedFromAst()) {
                val subChildren = child.childStorage
                for (i in 0 until subChildren.size) {
                    appendChild(subChildren[i])
                }
            } else {
                appendChild(child)
            }
        }
    }

    private fun appendChild(child: AstNode) {
        val current = childStorage
        val index = current.size
        when {
            current is GrowableChildArray -> current.add(child)
            index == 0 -> childStorage = SingleChild(child)
            else -> childStorage = GrowableChildArray(current, child)
        }
        child.attachTo(this, index)
    }

    private fun attachTo(parent: AstNode, index: Int) {
        childIndex = index
        parentOrNull = parent
    }

    /**
     * Sets the children of this node in one go, in storage of the exact size. The candidates are read from [from]
     * (inclusive) to [to] (exclusive) in [candidates]: null ones are ignored, and each other one is checked with
     * [hasToBeSkippedFromAst] and replaced by its own children when it has to be skipped, as [addChild] does.
     * A node that already has children gets the candidates appended with [addChild].
     */
    internal fun setChildren(candidates: List<AstNode?>, from: Int = 0, to: Int = candidates.size) {
        if (childStorage.isNotEmpty()) {
            for (i in from until to) {
                addChild(candidates[i])
            }
            return
        }
        var count = 0
        var skipped: BooleanArray? = null
        for (i in from until to) {
            val candidate = candidates[i] ?: continue
            if (candidate.hasToBeSkippedFromAst()) {
                if (skipped == null) {
                    skipped = BooleanArray(to - from)
                }
                skipped[i - from] = true
                count += candidate.childStorage.size
            } else {
                count++
            }
        }
        if (count == 0) {
            return
        }
        if (count == 1) {
            for (i in from until to) {
                val candidate = candidates[i] ?: continue
                val only = if (skipped != null && skipped[i - from]) {
                    candidate.childStorage.firstOrNull() ?: continue
                } else {
                    candidate
                }
                childStorage = SingleChild(only)
                only.attachTo(this, 0)
                return
            }
        }
        val array = arrayOfNulls<AstNode>(count)
        var index = 0
        for (i in from until to) {
            val candidate = candidates[i] ?: continue
            if (skipped != null && skipped[i - from]) {
                val subChildren = candidate.childStorage
                for (j in 0 until subChildren.size) {
                    val subChild = subChildren[j]
                    array[index] = subChild
                    subChild.attachTo(this, index++)
                }
            } else {
                array[index] = candidate
                candidate.attachTo(this, index++)
            }
        }
        @Suppress("UNCHECKED_CAST")
        childStorage = ChildArray(array as Array<AstNode>)
    }

    /**
     * @return true if this AstNode has some children.
     */
    public fun hasChildren(): Boolean {
        return childStorage.isNotEmpty()
    }

    public val numberOfChildren: Int
        get() {
            return childStorage.size
        }

    /**
     * Get the next sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
     *
     * @since 1.17
     */
    public val nextAstNodeOrNUll: AstNode?
        get() {
            return nextSiblingOrNull ?: parentOrNull?.nextAstNode
        }

    public val nextAstNode: AstNode
        get() = checkNotNull(nextAstNodeOrNUll)

    /**
     * Get the previous sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
     *
     * @since 1.17
     */
    public val previousAstNodeOrNull: AstNode?
        get() {
            return previousSiblingOrNull ?: parentOrNull?.previousAstNode
        }

    public val previousAstNode: AstNode
        get() = checkNotNull(previousAstNodeOrNull)

    /**
     * Get the next sibling AstNode if exists in the tree.
     *
     * @return next sibling, or null if not exists
     * @since 1.17
     */
    public val nextSiblingOrNull: AstNode?
        get() {
            val parent = this.parentOrNull
            return if (parent != null && parent.numberOfChildren > childIndex + 1) {
                parent.children[childIndex + 1]
            } else null
        }

    public val nextSibling: AstNode
        get() = checkNotNull(nextSiblingOrNull)

    /**
     * Get the previous sibling AstNode if exists in the tree.
     *
     * @return previous sibling, or null if not exists
     * @since 1.17
     */
    public val previousSiblingOrNull: AstNode?
        get() {
            val parent = this.parentOrNull ?: return null
            return if (childIndex > 0) {
                parent.children[childIndex - 1]
            } else null
        }

    public val previousSibling: AstNode
        get() = checkNotNull(previousSiblingOrNull)

    /**
     * Get the Token's value associated to this AstNode
     *
     * @return token's value
     */
    public val tokenValue: String
        get() {
            return tokenOrNull?.value.orEmpty()
        }

    /**
     * Get the Token's original value associated to this AstNode
     *
     * @return token's original value
     */
    public open val tokenOriginalValue: String
        get() {
            return tokenOrNull?.originalValue.orEmpty()
        }

    /**
     * Get the Token's line associated to this AstNode
     *
     * @return token's line
     */
    public val tokenLine: Int
        get() {
            return tokenOrNull?.line ?: -1
        }

    public fun hasToken(): Boolean {
        return tokenOrNull != null
    }

    /**
     * For internal use only.
     */
    public fun hasToBeSkippedFromAst(): Boolean {
        val internalType = type

        val result = (internalType as? AstNodeSkippingPolicy)?.hasToBeSkippedFromAst(this) ?: false

        // For LexerlessGrammarBuilder and LexerfulGrammarBuilder
        // unwrap AstNodeType to get a real one, i.e. detach node from tree of matchers:
        when (internalType) {
            is MutableParsingRule -> {
                type = internalType.getRealAstNodeType()
            }
            is RuleDefinition -> {
                type = internalType.getRealAstNodeType()
            }
        }
        return result
    }

    public fun `is`(vararg types: AstNodeType): Boolean = isAnyOf(types)

    public fun isNot(vararg types: AstNodeType): Boolean = !isAnyOf(types)

    // The walks below pass the caller's array on as it is: spreading a vararg array into another call copies it.
    private fun isAnyOf(types: Array<out AstNodeType>): Boolean = types.any { type === it }

    /**
     * Returns first child of one of specified types.
     *
     *
     * In the following case, `getFirstChild("B")` would return "B2":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     *
     * @return first child of one of specified types
     * @throws [NoSuchElementException] if no such element is found.
     * @since 1.17
     */
    public fun getFirstChild(vararg nodeTypes: AstNodeType): AstNode {
        val children = childStorage
        for (i in 0 until children.size) {
            val child = children[i]
            if (nodeTypes.isEmpty() || child.type in nodeTypes) return child
        }
        throw NoSuchElementException("List contains no element matching the predicate.")
    }

    /**
     * Returns first child of one of specified types.
     *
     *
     * In the following case, `getFirstChild("B")` would return "B2":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     *
     * @return first child of one of specified types, or null if not found
     * @since 1.17
     */
    public fun getFirstChildOrNull(vararg nodeTypes: AstNodeType): AstNode? {
        val children = childStorage
        for (i in 0 until children.size) {
            val child = children[i]
            if (nodeTypes.isEmpty() || child.type in nodeTypes) return child
        }
        return null
    }

    /**
     * Returns first descendant of one of specified types.
     *
     *
     * In the following case, `getFirstDescendant("B")` would return "B1":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     *
     * @return first descendant of one of specified types, or null if not found
     * @since 1.17
     */
    public fun getFirstDescendantOrNull(vararg nodeTypes: AstNodeType): AstNode? = firstDescendantOrNull(nodeTypes)

    private fun firstDescendantOrNull(nodeTypes: Array<out AstNodeType>): AstNode? {
        val children = childStorage
        for (i in 0 until children.size) {
            val child = children[i]
            if (child.isAnyOf(nodeTypes)) {
                return child
            }
            val node = child.firstDescendantOrNull(nodeTypes)
            if (node != null) {
                return node
            }
        }
        return null
    }

    public fun getFirstDescendant(vararg nodeTypes: AstNodeType): AstNode =
        checkNotNull(firstDescendantOrNull(nodeTypes))

    /**
     * Returns the first child of this node.
     *
     * @return the first child
     * @throws [NoSuchElementException] if no such element is found.
     */
    public val firstChild: AstNode
        get() {
            return children.first()
        }

    /**
     * Returns the first child of this node.
     *
     * @return the first child, or null if there is no child
     */
    public val firstChildOrNull: AstNode?
        get() {
            return children.firstOrNull()
        }

    /**
     * Returns children of specified types.
     * In the following case, `getChildren("B")` would return "B2" and "B3":
     *
     *
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
    </pre> *
     *
     * @return children of specified types, never null
     * @since 1.17
     */
    public fun getChildren(vararg nodeTypes: AstNodeType): List<AstNode> {
        return children.filter { nodeTypes.isEmpty() || it.type in nodeTypes }
    }

    /**
     * Returns descendants of specified types.
     * Be careful, this method searches among all descendants whatever is their depth, so favor [.getChildren] when possible.
     *
     *
     * In the following case, `getDescendants("B", "C")` would return "C1", "B1", "B2" and "B3":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ D1
     * |__ B3
    </pre> *
     *
     * @return descendants of specified types, never null
     * @since 1.17
     */
    public fun getDescendants(vararg nodeTypes: AstNodeType): List<AstNode> {
        val result = mutableListOf<AstNode>()
        val children = childStorage
        for (i in 0 until children.size) {
            children[i].collectDescendants(result, nodeTypes)
        }
        return result
    }

    private fun collectDescendants(result: MutableList<AstNode>, nodeTypes: Array<out AstNodeType>) {
        for (nodeType in nodeTypes) {
            if (type === nodeType) {
                result.add(this)
            }
        }
        val children = childStorage
        for (i in 0 until children.size) {
            children[i].collectDescendants(result, nodeTypes)
        }
    }

    /**
     * Returns the last child of this node.
     *
     * @return the last child
     * @throws [NoSuchElementException] if no such element is found.
     */
    public val lastChild: AstNode
        get() {
            return children.last()
        }

    /**
     * Returns the last child of this node.
     *
     * @return the last child, or null if there is no child
     */
    public val lastChildOrNull: AstNode?
        get() {
            return children.lastOrNull()
        }

    /**
     * Returns last child of one of specified types.
     *
     *
     * In the following case, `getLastChild("B")` would return "B3":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
     * |__ B4
    </pre> *
     *
     * @return last child of one of specified types
     * @throws [NoSuchElementException] if no such element is found.
     * @since 1.20
     */
    public fun getLastChild(vararg nodeTypes: AstNodeType): AstNode {
        return children.last { nodeTypes.isEmpty() || it.type in nodeTypes }
    }

    /**
     * Returns last child of one of specified types.
     *
     *
     * In the following case, `getLastChild("B")` would return "B3":
     * <pre>
     * A1
     * |__ C1
     * |    |__ B1
     * |__ B2
     * |__ B3
     * |__ B4
    </pre> *
     *
     * @return last child of one of specified types, or null if not found
     * @since 1.20
     */
    public fun getLastChildOrNull(vararg nodeTypes: AstNodeType): AstNode? {
        return children.lastOrNull { nodeTypes.isEmpty() || it.type in nodeTypes }
    }

    /**
     * @return true if this node has some children with the requested node types
     */
    public fun hasDirectChildren(vararg nodeTypes: AstNodeType): Boolean {
        return getFirstChildOrNull(*nodeTypes) != null
    }

    /**
     * @return true if this node has a descendant of one of specified types
     * @since 1.17
     */
    public fun hasDescendant(vararg nodeTypes: AstNodeType): Boolean {
        return firstDescendantOrNull(nodeTypes) != null
    }

    /**
     * @since 1.19.2
     */
    public fun hasParent(vararg nodeTypes: AstNodeType): Boolean {
        val parent = this.parentOrNull
        return parent != null && parent.isAnyOf(nodeTypes)
    }

    /**
     * @return true if this node has an ancestor of the specified type
     * @since 1.17
     */
    public fun hasAncestor(nodeType: AstNodeType): Boolean {
        return getFirstAncestorOrNull(nodeType) != null
    }

    /**
     * @return true if this node has an ancestor of one of specified types
     * @since 1.19.2
     */
    public fun hasAncestor(vararg nodeTypes: AstNodeType): Boolean {
        return firstAncestorOrNull(nodeTypes) != null
    }

    /**
     * @return first ancestor of the specified type, or null if not found
     * @since 1.17
     */
    public fun getFirstAncestorOrNull(nodeType: AstNodeType): AstNode? {
        val parent = this.parentOrNull
        return when {
            parent == null -> {
                null
            }
            parent.type === nodeType -> {
                parent
            }
            else -> {
                parent.getFirstAncestorOrNull(nodeType)
            }
        }
    }

    public fun getFirstAncestor(nodeType: AstNodeType): AstNode = checkNotNull(getFirstAncestorOrNull(nodeType))

    /**
     * @return first ancestor of one of specified types, or null if not found
     * @since 1.19.2
     */
    public fun getFirstAncestorOrNull(vararg nodeTypes: AstNodeType): AstNode? = firstAncestorOrNull(nodeTypes)

    private fun firstAncestorOrNull(nodeTypes: Array<out AstNodeType>): AstNode? {
        var result = parentOrNull
        while (result != null) {
            if (result.isAnyOf(nodeTypes)) {
                return result
            }
            result = result.parentOrNull
        }
        return null
    }

    public fun getFirstAncestor(vararg nodeTypes: AstNodeType): AstNode = checkNotNull(firstAncestorOrNull(nodeTypes))

    /**
     * Return all tokens contained in this tree node. Those tokens can be directly or indirectly attached to this node.
     */
    public val tokens: List<Token>
        get() {
            val tokens = mutableListOf<Token>()
            getTokens(tokens)
            return tokens
        }

    private fun getTokens(tokens: MutableList<Token>) {
        if (!hasChildren()) {
            if (tokenOrNull != null) {
                tokens.add(token)
            }
        } else {
            val children = childStorage
            for (i in 0 until children.size) {
                children[i].getTokens(tokens)
            }
        }
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append(name)
        if (tokenOrNull != null) {
            result.append(" tokenValue='").append(token.value).append("'")
            result.append(" tokenLine=").append(token.line)
            result.append(" tokenColumn=").append(token.column)
        }
        return result.toString()
    }

    public val lastTokenOrNull: Token?
        get() {
            if (!hasToken()) {
                return null
            }
            var currentNode = this
            while (currentNode.hasChildren()) {
                val children = currentNode.childStorage
                var i = children.size - 1
                while (i >= 0 && !children[i].hasToken()) {
                    i--
                }
                if (i < 0) throw NoSuchElementException("List contains no element matching the predicate.")
                currentNode = children[i]
            }
            return currentNode.token
        }

    public val lastToken: Token
        get() = checkNotNull(lastTokenOrNull)
}
