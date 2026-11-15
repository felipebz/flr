//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
