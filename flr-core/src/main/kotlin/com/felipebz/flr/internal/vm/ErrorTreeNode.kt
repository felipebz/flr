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
package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.matchers.MatcherPathElement

public class ErrorTreeNode {
    public var pathElement: MatcherPathElement? = null
    public var children: MutableList<ErrorTreeNode> = mutableListOf()

    public companion object {
        public fun buildTree(paths: List<List<MatcherPathElement>>): ErrorTreeNode {
            val root = ErrorTreeNode()
            root.pathElement = paths[0][0]
            for (path in paths) {
                addToTree(root, path)
            }
            return root
        }

        private fun addToTree(root: ErrorTreeNode, path: List<MatcherPathElement>) {
            var current = root
            var i = 1
            var found = true
            while (found && i < path.size) {
                found = false
                for (child in current.children) {
                    if (child.pathElement == path[i]) {
                        current = child
                        i++
                        found = true
                        break
                    }
                }
            }
            while (i < path.size) {
                val child = ErrorTreeNode()
                child.pathElement = path[i]
                current.children.add(child)
                current = child
                i++
            }
        }
    }
}