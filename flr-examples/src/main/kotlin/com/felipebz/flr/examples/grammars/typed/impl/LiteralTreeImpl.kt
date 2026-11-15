//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.impl

import com.felipebz.flr.examples.grammars.typed.api.LiteralTree
import com.felipebz.flr.examples.grammars.typed.api.SyntaxToken

public class LiteralTreeImpl(private val token: SyntaxToken) : LiteralTree {
    override fun token(): SyntaxToken {
        return token
    }
}
