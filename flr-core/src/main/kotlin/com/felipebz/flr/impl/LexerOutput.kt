/**
 * FLR
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
package com.felipebz.flr.impl

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.Trivia
import java.net.URI
import java.util.*

public class LexerOutput(public val uri: URI = URI("tests://unittest")) {

    private val trivia = LinkedList<Trivia>()
    private var _tokens = mutableListOf<Token>()
    private val strings = SharedStrings()
    private val tokenText = TokenTextBuilder()

    public val tokens: List<Token>
        get() = _tokens.toList()

    public val tokenCount: Int
        get() = _tokens.size

    public val lastToken: Token?
        get() = _tokens.lastOrNull()

    public fun tokenAtOrNull(index: Int): Token? =
        _tokens.getOrNull(index)

    /**
     * A string equal to [text]: the one this output already gave for an equal text, or a new one that it gives for
     * the next equal texts. Channels use it for the values of the tokens and trivia they create, so that the tokens of
     * one lexed source share their strings; a text built in [tokenText] is found without creating a string. The
     * strings are kept only as long as this output, and only one thread may use it, as for the rest of the output.
     */
    public fun sharedString(text: CharSequence): String =
        strings.share(text)

    /**
     * What [compute] makes from the string that [sharedString] gives for [text], made for the first text equal to it
     * with this [key]: later equal texts with the same key get that same result without calling [compute] again. It
     * is meant for what depends only on the text, such as the value and type of a word, with the channel as the key;
     * one result is kept per text, so another key computes and replaces it.
     */
    public fun <T : Any> sharedResult(text: CharSequence, key: Any, compute: LexerOutput.(String) -> T): T =
        strings.shareResult(text, key, this, compute)

    /**
     * The builder in which a channel appends the characters of the token it reads, before giving it to
     * [sharedString]. This output has a single one: each call empties it and returns it again, so a channel uses it
     * for one text at a time.
     */
    public fun tokenText(): TokenTextBuilder {
        tokenText.clear()
        return tokenText
    }

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
