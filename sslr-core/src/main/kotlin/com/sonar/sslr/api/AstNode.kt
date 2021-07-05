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
package com.sonar.sslr.api

import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.grammar.MutableParsingRule

/**
 * the parser is in charge to construct an abstract syntax tree (AST) which is a tree representation of the abstract syntactic structure of
 * source code. Each node of the tree is an AstNode and each node denotes a construct occurring in the source code which starts at a given
 * Token.
 *
 * @see Token
 */
open class AstNode(
    private var _type: AstNodeType?, val name: String?,
    /**
     * Get the Token associated to this AstNode
     */
    token: Token?
) {
    private val _token = token

    val token: Token
        get() = _token.let {
            checkNotNull(it)
            return it
        }

    val type: AstNodeType
        get() = _type.let {
            checkNotNull(it)
            return it
        }

    /**
     * Get the list of children.
     *
     * @return list of children
     */
    var children: MutableList<AstNode> = mutableListOf()
        private set
    private var childIndex = -1

    /**
     * Get the parent of this node in the tree.
     */
    var parent: AstNode? = null
        private set
    var fromIndex = 0
    var toIndex = 0

    constructor(token: Token) : this(token.type, token.type.name, token)

    fun addChild(child: AstNode?) {
        if (child != null) {
            if (children.isEmpty()) {
                children = ArrayList()
            }
            if (child.hasToBeSkippedFromAst()) {
                if (child.hasChildren()) {
                    for (subChild in child.children) {
                        addChildToList(subChild)
                    }
                }
            } else {
                addChildToList(child)
            }
        }
    }

    private fun addChildToList(child: AstNode) {
        children.add(child)
        child.childIndex = children.size - 1
        child.parent = this
    }

    /**
     * @return true if this AstNode has some children.
     */
    fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    val numberOfChildren: Int 
        get()
        {
            return children.size
        }

    /**
     * Get the next sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
     *
     * @since 1.17
     */
    val nextAstNode: AstNode? 
        get() {
            return nextSibling ?: parent?.nextAstNode
        }

    /**
     * Get the previous sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
     *
     * @since 1.17
     */
    val previousAstNode: AstNode?
        get() {
            return previousSibling ?: parent?.previousAstNode
        }

    /**
     * Get the next sibling AstNode if exists in the tree.
     *
     * @return next sibling, or null if not exists
     * @since 1.17
     */
    val nextSibling: AstNode?
        get() {
            val parent = this.parent
            return if (parent != null && parent.numberOfChildren > childIndex + 1) {
                parent.children[childIndex + 1]
            } else null
        }

    /**
     * Get the previous sibling AstNode if exists in the tree.
     *
     * @return previous sibling, or null if not exists
     * @since 1.17
     */
    val previousSibling: AstNode?
        get() {
            val parent = this.parent ?: return null
            return if (childIndex > 0) {
                parent.children[childIndex - 1]
            } else null
        }

    /**
     * Get the Token's value associated to this AstNode
     *
     * @return token's value
     */
    val tokenValue: String
        get() {
            return _token?.value.orEmpty()
        }

    /**
     * Get the Token's original value associated to this AstNode
     *
     * @return token's original value
     */
    open val tokenOriginalValue: String
        get() {
            return _token?.originalValue.orEmpty()
        }

    /**
     * Get the Token's line associated to this AstNode
     *
     * @return token's line
     */
    val tokenLine: Int
        get() {
            return _token?.line ?: -1
        }

    fun hasToken(): Boolean {
        return _token != null
    }

    /**
     * For internal use only.
     */
    fun hasToBeSkippedFromAst(): Boolean {
        if (_type == null) {
            return true
        }
        val result = if (AstNodeSkippingPolicy::class.java.isAssignableFrom(type.javaClass)) {
            (type as AstNodeSkippingPolicy).hasToBeSkippedFromAst(this)
        } else {
            false
        }
        // For LexerlessGrammarBuilder and LexerfulGrammarBuilder
        // unwrap AstNodeType to get a real one, i.e. detach node from tree of matchers:
        if (type is MutableParsingRule) {
            _type = (type as MutableParsingRule).getRealAstNodeType()
        } else if (type is RuleDefinition) {
            _type = (type as RuleDefinition).getRealAstNodeType()
        }
        return result
    }

    fun `is`(vararg types: AstNodeType): Boolean {
        for (expectedType in types) {
            if (type === expectedType) {
                return true
            }
        }
        return false
    }

    fun isNot(vararg types: AstNodeType): Boolean {
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
     * @return first child of one of specified types, or null if not found
     * @since 1.17
     */
    fun getFirstChild(vararg nodeTypes: AstNodeType): AstNode? {
        for (child in children) {
            for (nodeType in nodeTypes) {
                if (child.type === nodeType) {
                    return child
                }
            }
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
    fun getFirstDescendant(vararg nodeTypes: AstNodeType): AstNode? {
        for (child in children) {
            if (child.`is`(*nodeTypes)) {
                return child
            }
            val node = child.getFirstDescendant(*nodeTypes)
            if (node != null) {
                return node
            }
        }
        return null
    }

    /**
     * Returns the first child of this node.
     *
     * @return the first child, or null if there is no child
     */
    val firstChild: AstNode?
        get() {
            return if (children.isEmpty()) null else children[0]
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
    fun getChildren(vararg nodeTypes: AstNodeType): List<AstNode> {
        val result: MutableList<AstNode> = ArrayList()
        for (child in children) {
            for (nodeType in nodeTypes) {
                if (child.type === nodeType) {
                    result.add(child)
                }
            }
        }
        return result
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
    fun getDescendants(vararg nodeTypes: AstNodeType): MutableList<AstNode> {
        val result: MutableList<AstNode> = ArrayList()
        if (hasChildren()) {
            for (child in children) {
                child.getDescendants(result, *nodeTypes)
            }
        }
        return result
    }

    private fun getDescendants(result: MutableList<AstNode>, vararg nodeTypes: AstNodeType) {
        for (nodeType in nodeTypes) {
            if (`is`(nodeType)) {
                result.add(this)
            }
        }
        if (hasChildren()) {
            for (child in children) {
                child.getDescendants(result, *nodeTypes)
            }
        }
    }

    /**
     * Returns the last child of this node.
     *
     * @return the last child, or null if there is no child
     */
    val lastChild: AstNode?
        get() {
            return if (children.isEmpty()) null else children[children.size - 1]
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
    fun getLastChild(vararg nodeTypes: AstNodeType): AstNode? {
        for (i in children.indices.reversed()) {
            val child = children[i]
            for (nodeType in nodeTypes) {
                if (child.type === nodeType) {
                    return child
                }
            }
        }
        return null
    }

    /**
     * @return true if this node has some children with the requested node types
     */
    fun hasDirectChildren(vararg nodeTypes: AstNodeType): Boolean {
        return getFirstChild(*nodeTypes) != null
    }

    /**
     * @return true if this node has a descendant of one of specified types
     * @since 1.17
     */
    fun hasDescendant(vararg nodeTypes: AstNodeType): Boolean {
        return getFirstDescendant(*nodeTypes) != null
    }

    /**
     * @since 1.19.2
     */
    fun hasParent(vararg nodeTypes: AstNodeType): Boolean {
        val parent = this.parent
        return parent != null && parent.`is`(*nodeTypes)
    }

    /**
     * @return true if this node has an ancestor of the specified type
     * @since 1.17
     */
    fun hasAncestor(nodeType: AstNodeType): Boolean {
        return getFirstAncestor(nodeType) != null
    }

    /**
     * @return true if this node has an ancestor of one of specified types
     * @since 1.19.2
     */
    fun hasAncestor(vararg nodeTypes: AstNodeType): Boolean {
        return getFirstAncestor(*nodeTypes) != null
    }

    /**
     * @return first ancestor of the specified type, or null if not found
     * @since 1.17
     */
    fun getFirstAncestor(nodeType: AstNodeType): AstNode? {
        val parent = this.parent
        return when {
            parent == null -> {
                null
            }
            parent.`is`(nodeType) -> {
                parent
            }
            else -> {
                parent.getFirstAncestor(nodeType)
            }
        }
    }

    /**
     * @return first ancestor of one of specified types, or null if not found
     * @since 1.19.2
     */
    fun getFirstAncestor(vararg nodeTypes: AstNodeType): AstNode? {
        var result = parent
        while (result != null) {
            if (result.`is`(*nodeTypes)) {
                return result
            }
            result = result.parent
        }
        return null
    }

    val isCopyBookOrGeneratedNode: Boolean
        get() {
            return checkNotNull(_token).isCopyBook || _token.isGeneratedCode
        }

    /**
     * Return all tokens contained in this tree node. Those tokens can be directly or indirectly attached to this node.
     */
    val tokens: List<Token>
        get() {
            val tokens: MutableList<Token> = ArrayList()
            getTokens(tokens)
            return tokens
        }

    private fun getTokens(tokens: MutableList<Token>) {
        if (!hasChildren()) {
            if (_token != null) {
                tokens.add(token)
            }
        } else {
            for (i in children.indices) {
                children[i].getTokens(tokens)
            }
        }
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append(name)
        if (_token != null) {
            result.append(" tokenValue='").append(token.value).append("'")
            result.append(" tokenLine=").append(token.line)
            result.append(" tokenColumn=").append(token.column)
        }
        return result.toString()
    }

    val lastToken: Token?
        get() {
            if (!hasToken()) {
                return null
            }
            var currentNode = this
            while (currentNode.hasChildren()) {
                for (i in currentNode.children.indices.reversed()) {
                    val child = currentNode.children[i]
                    if (child.hasToken()) {
                        currentNode = child
                        break
                    }
                }
            }
            return currentNode.token
        }
}