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
package com.felipebz.flr.internal.matchers

import java.util.*

public class MatcherPathElement(private val matcher: Matcher, private val startIndex: Int, private val endIndex: Int) {
    public fun getMatcher(): Matcher {
        return matcher
    }

    public fun getStartIndex(): Int {
        return startIndex
    }

    public fun getEndIndex(): Int {
        return endIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(matcher, startIndex, endIndex)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is MatcherPathElement) {
            return matcher == other.matcher && startIndex == other.startIndex && endIndex == other.endIndex
        }
        return false
    }
}