//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.impl

import com.felipebz.flr.examples.grammars.typed.api.ObjectTree
import com.felipebz.flr.examples.grammars.typed.api.PairTree
import com.felipebz.flr.examples.grammars.typed.api.SyntaxToken

public class ObjectTreeImpl(
    private val openCurlyBraceToken: SyntaxToken,
    private val pairs: SyntaxList<PairTree?>?,
    private val closeCurlyBraceToken: SyntaxToken
) : ObjectTree {
    override fun openCurlyBraceToken(): SyntaxToken {
        return openCurlyBraceToken
    }

    override fun pairs(): SyntaxList<PairTree?>? {
        return pairs
    }

    override fun closeCurlyBraceToken(): SyntaxToken {
        return closeCurlyBraceToken
    }
}
