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
package com.sonar.sslr.test.lexer

import com.sonar.sslr.api.Token
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

internal class HasTokensMatcher(private vararg val tokenValues: String) : BaseMatcher<List<Token>>() {
    override fun matches(obj: Any?): Boolean {
        if (obj !is MutableList<*>) {
            return false
        }
        val tokens = obj as MutableList<Token>
        for (i in tokens.indices) {
            val token = tokens[i]
            if (token.value != tokenValues[i]) {
                return false
            }
        }
        return tokenValues.size == tokens.size
    }

    override fun describeTo(desc: Description) {
        desc.appendText(tokenValues.size.toString() + " tokens(")
        for (i in tokenValues.indices) {
            desc.appendText("'" + tokenValues[i] + "'")
            if (i < tokenValues.size - 1) {
                desc.appendText(",")
            }
        }
        desc.appendText(")")
    }

}