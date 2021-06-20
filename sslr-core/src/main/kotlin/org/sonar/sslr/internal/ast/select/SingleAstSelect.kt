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
package org.sonar.sslr.internal.ast.select

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeType
import org.sonar.sslr.ast.AstSelect
import java.util.function.Predicate

/**
 * [AstSelect] which contains exactly one element.
 */
class SingleAstSelect(private val node: AstNode) : AstSelect {
    override fun children(): AstSelect {
        return when {
            node.numberOfChildren == 1 -> {
                SingleAstSelect(checkNotNull(node.firstChild))
            }
            node.numberOfChildren > 1 -> {
                ListAstSelect(node.children)
            }
            else -> {
                AstSelectFactory.empty()
            }
        }
    }

    override fun children(type: AstNodeType): AstSelect {
        return if (node.numberOfChildren == 1) {
            val result = node.children[0]
            if (result.type === type) {
                SingleAstSelect(result)
            } else AstSelectFactory.empty()
        } else if (node.numberOfChildren > 1) {
            val result: MutableList<AstNode> = ArrayList()
            // Don't use "getChildren(type)", because under the hood it will create an array of types and new List to keep the result
            for (child in node.children) {
                // Don't use "is(type)", because under the hood it will create an array of types
                if (child.type === type) {
                    result.add(child)
                }
            }
            AstSelectFactory.create(result)
        } else {
            AstSelectFactory.empty()
        }
    }

    override fun children(vararg types: AstNodeType): AstSelect {
        return if (node.numberOfChildren == 1) {
            val result = node.children[0]
            if (result.`is`(*types)) {
                SingleAstSelect(result)
            } else AstSelectFactory.empty()
        } else if (node.numberOfChildren > 1) {
            val result: MutableList<AstNode> = ArrayList()
            // Don't use "getChildren(type)", because it will create new List to keep the result
            for (child in node.children) {
                if (child.`is`(*types)) {
                    result.add(child)
                }
            }
            AstSelectFactory.create(result)
        } else {
            AstSelectFactory.empty()
        }
    }

    override fun nextSibling(): AstSelect {
        return AstSelectFactory.select(node.nextSibling)
    }

    override fun previousSibling(): AstSelect {
        return AstSelectFactory.select(node.previousSibling)
    }

    override fun parent(): AstSelect {
        return AstSelectFactory.select(node.parent)
    }

    override fun firstAncestor(type: AstNodeType): AstSelect {
        var result = node.parent
        while (result != null && result.type !== type) {
            result = result.parent
        }
        return AstSelectFactory.select(result)
    }

    override fun firstAncestor(vararg types: AstNodeType): AstSelect {
        var result = node.parent
        while (result != null && !result.`is`(*types)) {
            result = result.parent
        }
        return AstSelectFactory.select(result)
    }

    override fun descendants(type: AstNodeType): AstSelect {
        return AstSelectFactory.create(node.getDescendants(type))
    }

    override fun descendants(vararg types: AstNodeType): AstSelect {
        return AstSelectFactory.create(node.getDescendants(*types))
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun isNotEmpty(): Boolean {
        return true
    }

    override fun filter(type: AstNodeType): AstSelect {
        return if (node.type === type) this else AstSelectFactory.empty()
    }

    override fun filter(vararg types: AstNodeType): AstSelect {
        return if (node.`is`(*types)) this else AstSelectFactory.empty()
    }

    override fun filter(predicate: Predicate<AstNode>): AstSelect {
        return if (predicate.test(node)) this else AstSelectFactory.empty()
    }

    override fun size(): Int {
        return 1
    }

    override fun get(index: Int): AstNode {
        if (index == 0) {
            return node
        }
        throw IndexOutOfBoundsException()
    }

    override fun iterator(): Iterator<AstNode> {
        return setOf(node).requireNoNulls().iterator()
    }
}