//
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.Token

public fun interface TerminalNodeBuilder {
    public fun build(token: Token): AstNode
}
