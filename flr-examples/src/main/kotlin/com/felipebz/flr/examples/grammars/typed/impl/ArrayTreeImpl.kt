//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.impl

import com.felipebz.flr.examples.grammars.typed.api.ArrayTree
import com.felipebz.flr.examples.grammars.typed.api.SyntaxToken
import com.felipebz.flr.examples.grammars.typed.api.ValueTree

public class ArrayTreeImpl(
    private val openBracketToken: SyntaxToken?,
    private val values: SyntaxList<ValueTree?>?,
    private val closeBracketToken: SyntaxToken?
) : ArrayTree {
    override fun openBracketToken(): SyntaxToken? {
        return openBracketToken
    }

    override fun values(): SyntaxList<ValueTree?>? {
        return values
    }

    override fun closeBracketToken(): SyntaxToken? {
        return closeBracketToken
    }
}
