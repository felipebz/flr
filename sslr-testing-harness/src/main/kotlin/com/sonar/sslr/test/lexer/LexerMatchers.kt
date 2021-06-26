/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
import com.sonar.sslr.api.TokenType
import org.hamcrest.Matcher

object LexerMatchers {
    @JvmStatic
    fun hasToken(tokenValue: String, tokenType: TokenType): Matcher<List<Token>> {
        return HasTokenMatcher(tokenValue, tokenType)
    }

    @JvmStatic
    fun hasToken(tokenValue: String): Matcher<List<Token>> {
        return HasTokenValueMatcher(tokenValue)
    }

    @JvmStatic
    fun hasOriginalToken(tokenValue: String): Matcher<List<Token>> {
        return HasTokenValueMatcher(tokenValue, true)
    }

    @JvmStatic
    fun hasToken(tokenType: TokenType): Matcher<List<Token>> {
        return HasTokenTypeMatcher(tokenType)
    }

    @JvmStatic
    fun hasTokens(vararg tokenValues: String): Matcher<List<Token>> {
        return HasTokensMatcher(*tokenValues)
    }

    @JvmStatic
    fun hasLastToken(tokenValue: String, tokenType: TokenType): Matcher<List<Token>> {
        return HasLastTokenMatcher(tokenValue, tokenType)
    }

    @JvmStatic
    fun hasComment(commentValue: String): Matcher<List<Token>> {
        return HasCommentMatcher(commentValue)
    }

    @JvmStatic
    fun hasComment(commentValue: String, commentLine: Int): Matcher<List<Token>> {
        return HasCommentMatcher(commentValue, commentLine)
    }

    @JvmStatic
    fun hasOriginalComment(commentValue: String): Matcher<List<Token>> {
        return HasCommentMatcher(commentValue, true)
    }

    @JvmStatic
    fun hasOriginalComment(commentValue: String, commentLine: Int): Matcher<List<Token>> {
        return HasCommentMatcher(commentValue, commentLine, true)
    }
}