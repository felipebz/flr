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
 * [AstSelect] which contains more than one element.
 */
class ListAstSelect(private val list: List<AstNode>) : AstSelect {
    override fun children(): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            result.addAll(node.children)
        }
        return AstSelectFactory.create(result)
    }

    override fun children(type: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            // Don't use "getChildren(type)", because under the hood it will create an array of types and new List to keep the result
            for (child in node.children) {
                // Don't use "is(type)", because under the hood it will create an array of types
                if (child.type === type) {
                    result.add(child)
                }
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun children(vararg types: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            // Don't use "getChildren(type)", because it will create new List to keep the result
            for (child in node.children) {
                if (child.`is`(*types)) {
                    result.add(child)
                }
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun nextSibling(): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            node.nextSibling?.let { result.add(it) }
        }
        return AstSelectFactory.create(result)
    }

    override fun previousSibling(): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            node.previousSibling?.let { result.add(it) }
        }
        return AstSelectFactory.create(result)
    }

    override fun parent(): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            val node = node.parent
            if (node != null) {
                result.add(node)
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun firstAncestor(type: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            var node = node.parent
            while (node != null && node.type !== type) {
                node = node.parent
            }
            if (node != null) {
                result.add(node)
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun firstAncestor(vararg types: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            var node = node.parent
            while (node != null && !node.`is`(*types)) {
                node = node.parent
            }
            if (node != null) {
                result.add(node)
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun descendants(type: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            result.addAll(node.getDescendants(type))
        }
        return AstSelectFactory.create(result)
    }

    override fun descendants(vararg types: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            result.addAll(node.getDescendants(*types))
        }
        return AstSelectFactory.create(result)
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun isNotEmpty(): Boolean {
        return true
    }

    override fun filter(type: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            if (node.type === type) {
                result.add(node)
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun filter(vararg types: AstNodeType): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            if (node.`is`(*types)) {
                result.add(node)
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun filter(predicate: Predicate<AstNode>): AstSelect {
        val result: MutableList<AstNode> = ArrayList()
        for (node in list) {
            if (predicate.test(node)) {
                result.add(node)
            }
        }
        return AstSelectFactory.create(result)
    }

    override fun size(): Int {
        return list.size
    }

    override fun get(index: Int): AstNode {
        return list[index]
    }

    override fun iterator(): Iterator<AstNode> {
        return list.iterator()
    }
}