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
package com.felipebz.flr.test.lexer

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

internal class HasLastTokenMatcher(private val tokenValue: String, private val tokenType: TokenType) :
    BaseMatcher<List<Token>>() {
    override fun matches(obj: Any?): Boolean {
        if (obj !is MutableList<*>) {
            return false
        }
        val tokens = obj as MutableList<Token>
        require(tokens.isNotEmpty()) { "There must be at least one lexed token." }
        val lastToken = tokens[tokens.size - 1]
        return lastToken.value == tokenValue && lastToken.type === tokenType
    }

    override fun describeTo(desc: Description) {
        desc.appendText("Token('$tokenValue',$tokenType)")
    }
}