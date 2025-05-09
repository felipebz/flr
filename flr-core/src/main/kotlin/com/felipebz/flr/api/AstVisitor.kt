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

public interface AstVisitor {
    /**
     * The AST node types that this check must be registered for.
     *
     * @return the AST node types this must be registered for.
     */
    public fun getAstNodeTypesToVisit(): List<AstNodeType>

    /**
     * Called before starting visiting a computation unit tree. Ideal place to initialize information that is to be collected while processing
     * the tree.
     *
     * @param ast
     * the root of the tree, or `null` if no tree
     */
    public fun visitFile(ast: AstNode?)

    /**
     * Called once a computation unit tree has been fully visited. Ideal place to report on information collected while processing a tree.
     *
     * @param ast
     * the root of the tree, or `null` if no tree
     */
    public fun leaveFile(ast: AstNode?)

    /**
     * Called to process an AST node whose type has been registered to be visited.
     *
     * @param ast
     * the AST node to process
     */
    public fun visitNode(ast: AstNode?)

    /**
     * Called once an AST node has been fully visited.
     *
     * @param ast
     * the AST node which has been visited
     */
    public fun leaveNode(ast: AstNode?)
}
