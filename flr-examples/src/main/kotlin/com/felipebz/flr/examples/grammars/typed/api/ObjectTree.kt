//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.api

import com.felipebz.flr.examples.grammars.typed.impl.SyntaxList

public interface ObjectTree : ValueTree {
    public fun openCurlyBraceToken(): SyntaxToken
    public fun pairs(): SyntaxList<PairTree?>?
    public fun closeCurlyBraceToken(): SyntaxToken
}
