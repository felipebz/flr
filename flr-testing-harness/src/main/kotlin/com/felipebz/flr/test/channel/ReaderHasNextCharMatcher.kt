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
package com.felipebz.flr.test.channel

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import com.felipebz.flr.channel.CodeReader

public class ReaderHasNextCharMatcher(private val nextChar: Char) : BaseMatcher<CodeReader?>() {
    override fun matches(arg0: Any?): Boolean {
        if (arg0 !is CodeReader) {
            return false
        }
        return arg0.peek() == nextChar.code
    }

    override fun describeTo(description: Description) {
        description.appendText("next char is '$nextChar'")
    }
}