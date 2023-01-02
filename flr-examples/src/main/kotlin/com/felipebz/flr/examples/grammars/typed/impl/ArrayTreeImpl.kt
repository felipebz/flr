/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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