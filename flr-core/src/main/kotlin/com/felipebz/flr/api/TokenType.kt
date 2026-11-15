//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

public interface TokenType : AstNodeType {
    public val name: String
    public val value: String
    public fun hasToBeSkippedFromAst(node: AstNode?): Boolean
}
