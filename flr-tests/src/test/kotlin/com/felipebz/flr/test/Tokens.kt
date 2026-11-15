//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.test

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType

/** A builder of a token of [type] and [value] at line 1, column 1. */
fun testTokenBuilder(type: TokenType, value: String): Token.Builder =
    Token.builder().setType(type).setValueAndOriginalValue(value).setLine(1).setColumn(1)

/** A token of [type] and [value] at line 1, column 1. */
fun testToken(type: TokenType, value: String): Token = testTokenBuilder(type, value).build()
