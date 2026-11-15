//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
