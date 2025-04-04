/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
     * Get the list of children.
     *
     * @return list of children
     */
    public val children: MutableList<AstNode> = mutableListOf()
    private var childIndex = -1

    /**
     * Get the parent of this node in the tree.
     */
    public var parentOrNull: AstNode? = null
        private set
    public val parent: AstNode get() = checkNotNull(parentOrNull)
    public var fromIndex: Int = 0
    public var toIndex: Int = 0

    public constructor(token: Token) : this(token.type, token.type.name, token)

    public fun addChild(child: AstNode?) {
        if (child != null) {
            if (child.hasToBeSkippedFromAst()) {
                for (subChild in child.children) {
                    addChildToList(subChild)
                }
            } else {
                addChildToList(child)
            }
        }
    }

    private fun addChildToList(child: AstNode) {
        children.add(child)
        child.childIndex = children.size - 1
        child.parentOrNull = this
    }

    /**
     * @return true if this AstNode has some children.
     */
    public fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    public val numberOfChildren: Int
        get() {
            return children.size
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

    public fun `is`(vararg types: AstNodeType): Boolean {
        return types.any { type === it }
    }

    public fun isNot(vararg types: AstNodeType): Boolean {
        return !`is`(*types)
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
     * @return first child of one of specified types
     * @throws [NoSuchElementException] if no such element is found.
     * @since 1.17
     */
    public fun getFirstChild(vararg nodeTypes: AstNodeType): AstNode {
        return children.first { nodeTypes.isEmpty() || it.type in nodeTypes }
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
        return children.firstOrNull { nodeTypes.isEmpty() || it.type in nodeTypes }
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
    public fun getFirstDescendantOrNull(vararg nodeTypes: AstNodeType): AstNode? {
        for (child in children) {
            if (child.`is`(*nodeTypes)) {
                return child
            }
            val node = child.getFirstDescendantOrNull(*nodeTypes)
            if (node != null) {
                return node
            }
        }
        return null
    }

    public fun getFirstDescendant(vararg nodeTypes: AstNodeType): AstNode =
        checkNotNull(getFirstDescendantOrNull(*nodeTypes))

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
        for (child in children) {
            child.getDescendants(result, *nodeTypes)
        }
        return result
    }

    private fun getDescendants(result: MutableList<AstNode>, vararg nodeTypes: AstNodeType) {
        for (nodeType in nodeTypes) {
            if (`is`(nodeType)) {
                result.add(this)
            }
        }
        for (child in children) {
            child.getDescendants(result, *nodeTypes)
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
        return getFirstDescendantOrNull(*nodeTypes) != null
    }

    /**
     * @since 1.19.2
     */
    public fun hasParent(vararg nodeTypes: AstNodeType): Boolean {
        val parent = this.parentOrNull
        return parent != null && parent.`is`(*nodeTypes)
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
        return getFirstAncestorOrNull(*nodeTypes) != null
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
            parent.`is`(nodeType) -> {
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
    public fun getFirstAncestorOrNull(vararg nodeTypes: AstNodeType): AstNode? {
        var result = parentOrNull
        while (result != null) {
            if (result.`is`(*nodeTypes)) {
                return result
            }
            result = result.parentOrNull
        }
        return null
    }

    public fun getFirstAncestor(vararg nodeTypes: AstNodeType): AstNode = checkNotNull(getFirstAncestorOrNull(*nodeTypes))

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
            for (i in children) {
                i.getTokens(tokens)
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
                currentNode = currentNode.children.last { it.hasToken() }
            }
            return currentNode.token
        }

    public val lastToken: Token
        get() = checkNotNull(lastTokenOrNull)
}
