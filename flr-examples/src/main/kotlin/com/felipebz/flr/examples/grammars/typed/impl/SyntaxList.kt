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