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

import com.felipebz.flr.api.*
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

internal class HasCommentMatcher @JvmOverloads constructor(
    private val commentValue: String,
    private val commentLine: Int,
    private val originalValue: Boolean = false
) : BaseMatcher<List<Token>>() {
    constructor(commentValue: String) : this(commentValue, -1)
    constructor(commentValue: String, originalValue: Boolean) : this(commentValue, -1, originalValue)

    override fun matches(obj: Any?): Boolean {
        if (obj !is MutableList<*>) {
            return false
        }
        val tokens = obj as MutableList<Token>
        for (token in tokens) {
            for (trivia in token.trivia) {
                if (trivia.isComment) {
                    val value: String = if (originalValue) trivia.token.originalValue else trivia.token.value
                    if (value == commentValue) {
                        if (commentLine > -1 && trivia.token.line != commentLine) {
                            continue
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun describeTo(desc: Description) {
        if (originalValue) {
            desc.appendText("Comment('$commentValue')")
        } else {
            desc.appendText("OriginalComment('$commentValue')")
        }
    }
}