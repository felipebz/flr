//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.impl

import com.felipebz.flr.examples.grammars.typed.api.SyntaxToken

public class SyntaxList<T>(private val element: T, private val commaToken: SyntaxToken?, private val next: SyntaxList<T>?) {
    public fun element(): T {
        return element
    }

    public fun commaToken(): SyntaxToken? {
        return commaToken
    }

    public operator fun next(): SyntaxList<*>? {
        return next
    }
}
