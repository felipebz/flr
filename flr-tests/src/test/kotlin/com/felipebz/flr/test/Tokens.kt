/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2026 Felipe Zorzo
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
package com.felipebz.flr.test

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType

/** A builder of a token of [type] and [value] at line 1, column 1. */
fun testTokenBuilder(type: TokenType, value: String): Token.Builder =
    Token.builder().setType(type).setValueAndOriginalValue(value).setLine(1).setColumn(1)

/** A token of [type] and [value] at line 1, column 1. */
fun testToken(type: TokenType, value: String): Token = testTokenBuilder(type, value).build()
