/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
 * mailto:felipe AT felipezorzo DOT com DOT br
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.sslr.examples.grammars.typed.impl

import org.sonar.sslr.examples.grammars.typed.api.ObjectTree
import org.sonar.sslr.examples.grammars.typed.api.PairTree
import org.sonar.sslr.examples.grammars.typed.api.SyntaxToken

class ObjectTreeImpl(
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