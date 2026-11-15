//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.TokenType

enum class MockTokenType : TokenType {
    WORD, WORD1, WORD2;

    override val value: String
        get() = name

    override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
        return false
    }
}
