//
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.Token

public fun interface NonTerminalNodeBuilder {
    public fun build(type: AstNodeType, name: String, token: Token?): AstNode
}
