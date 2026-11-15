//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.impl

import com.felipebz.flr.examples.grammars.typed.api.LiteralTree
import com.felipebz.flr.examples.grammars.typed.api.PairTree
import com.felipebz.flr.examples.grammars.typed.api.SyntaxToken
import com.felipebz.flr.examples.grammars.typed.api.ValueTree

public class PairTreeImpl(private val name: LiteralTree, private val colonToken: SyntaxToken, private val value: ValueTree) :
    PairTree {
    override fun name(): LiteralTree {
        return name
    }

    override fun colonToken(): SyntaxToken {
        return colonToken
    }

    override fun value(): ValueTree {
        return value
    }
}
