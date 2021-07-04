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

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.Lexer
import java.net.URI
import java.net.URISyntaxException

@Deprecated("in 1.17 All classes can now be mocked since none of them are final anymore, hence this helper is useless.")
object MockHelper {
    @JvmStatic
    fun mockLexer(): Lexer {
        return Lexer.builder().build()
    }

    @JvmStatic
    fun mockToken(type: TokenType, value: String): Token {
        return mockTokenBuilder(type, value).build()
    }

    @JvmStatic
    fun mockTokenBuilder(type: TokenType, value: String): Token.Builder {
        return try {
            Token.builder()
                .setType(type)
                .setValueAndOriginalValue(value)
                .setURI(URI("tests://unittest"))
                .setLine(1)
                .setColumn(1)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    fun mockAstNode(name: String): AstNode {
        return AstNode(GenericTokenType.IDENTIFIER, name, mockToken(GenericTokenType.LITERAL, "dummy"))
    }
}