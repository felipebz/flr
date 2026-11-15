//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

public enum class GenericTokenType : TokenType {
    COMMENT, IDENTIFIER, LITERAL, CONSTANT, EOF, EOL, UNKNOWN_CHAR;

    override val value: String
        get() {
            return name
        }

    override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
        return false
    }
}
