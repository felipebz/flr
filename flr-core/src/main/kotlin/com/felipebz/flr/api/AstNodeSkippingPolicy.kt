//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

/**
 * Specific Ast node types that can tell whether they should be skipped from being attached to the AST or not.
 *
 *
 * @see AstVisitor
 *
 * @see Grammar
 *
 * @see AstNode
 */
public interface AstNodeSkippingPolicy : AstNodeType {
    /**
     * Some AstNode can be pretty useless and makes a global AST less readable. This method allows to automatically remove those AstNode from
     * the AST.
     *
     * @param node
     * the node that should or not be removed from the AST
     * @return true if AstNode with this type must be skipped from the AST.
     */
    public fun hasToBeSkippedFromAst(node: AstNode): Boolean
}
