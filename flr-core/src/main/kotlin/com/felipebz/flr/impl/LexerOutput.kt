/**
 * FLR
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.impl

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.Trivia
import java.net.URI
import java.util.*

public class LexerOutput(public val uri: URI = URI("tests://unittest")) {

    private val trivia = LinkedList<Trivia>()
    private var _tokens = mutableListOf<Token>()

    public val tokens: List<Token>
        get() = _tokens.toList()

    public fun addTrivia(trivia: Trivia) {
        this.trivia.add(trivia)
    }

    public fun addToken(vararg tokens: Token) {
        require(tokens.isNotEmpty()) { "at least one token must be given" }
        val firstToken = tokens[0]
        val firstTokenWithTrivia: Token

        // Performance optimization: no need to rebuild token, if there is no trivia
        if (trivia.isEmpty() && !firstToken.hasTrivia()) {
            firstTokenWithTrivia = firstToken
        } else {
            firstTokenWithTrivia = Token.builder(firstToken).setTrivia(trivia).build()
            trivia.clear()
        }
        this._tokens.add(firstTokenWithTrivia)
        if (tokens.size > 1) {
            this._tokens.addAll(tokens.asList().subList(1, tokens.size))
        }
    }
}
