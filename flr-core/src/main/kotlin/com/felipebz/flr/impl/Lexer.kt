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
package com.felipebz.flr.impl

import com.felipebz.flr.api.*
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.ChannelDispatcher
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.channel.CodeReaderConfiguration
import java.io.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.util.*

public class Lexer private constructor(builder: Builder) {
    private val charset = builder.charset
    private val configuration = builder.configuration
    private val channelDispatcher = builder.channelDispatcher
    private val trivia: MutableList<Trivia> = LinkedList()
    private var _tokens = mutableListOf<Token>()

    public val tokens: List<Token>
        get() = _tokens.toList()

    public var uri: URI = URI("tests://unittest")
        private set

    public fun lex(file: File): List<Token> {
        Objects.requireNonNull(file, "file cannot be null")
        require(file.isFile) { "file \"" + file.absolutePath + "\" must be a file" }
        return try {
            lex(file.toURI().toURL())
        } catch (e: MalformedURLException) {
            throw LexerException("Unable to lex file: " + file.absolutePath, e)
        }
    }

    public fun lex(url: URL): List<Token> {
        Objects.requireNonNull(url, "url cannot be null")
        try {
            InputStreamReader(url.openStream(), charset).use { reader ->
                uri = url.toURI()
                return lex(reader)
            }
        } catch (e: Exception) {
            throw LexerException("Unable to lex url: $uri", e)
        }
    }

    /**
     * Do not use this method, it is intended for internal unit testing only
     *
     * @param sourceCode
     * @return
     */
    // @VisibleForTesting
    public fun lex(sourceCode: String): List<Token> {
        Objects.requireNonNull(sourceCode, "sourceCode cannot be null")
        return try {
            lex(StringReader(sourceCode))
        } catch (e: Exception) {
            throw LexerException("Unable to lex string source code \"$sourceCode\"", e)
        }
    }

    private fun lex(reader: Reader): List<Token> {
        _tokens = mutableListOf()
        val code = CodeReader(reader, configuration)
        return try {
            channelDispatcher.consume(code, this)
            addToken(
                Token.builder()
                    .setType(GenericTokenType.EOF)
                    .setValueAndOriginalValue("EOF")
                    .setURI(uri)
                    .setLine(code.getLinePosition())
                    .setColumn(code.getColumnPosition())
                    .build()
            )
            tokens
        } catch (e: Exception) {
            throw LexerException(
                "Unable to lex source code at line : " + code.getLinePosition() + " and column : "
                        + code.getColumnPosition() + " in file : " + uri, e
            )
        }
    }

    public fun addTrivia(vararg trivia: Trivia) {
        addTrivia(listOf(*trivia))
    }

    public fun addTrivia(trivia: List<Trivia>) {
        this.trivia.addAll(trivia)
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
            this._tokens.addAll(listOf(*tokens).subList(1, tokens.size))
        }
    }

    public class Builder {
        public var charset: Charset = Charset.defaultCharset()
        public val configuration: CodeReaderConfiguration = CodeReaderConfiguration()
        private val channels = mutableListOf<Channel<Lexer>>()
        private var failIfNoChannelToConsumeOneCharacter = false
        public fun build(): Lexer {
            return Lexer(this)
        }

        public fun withCharset(charset: Charset): Builder {
            this.charset = charset
            return this
        }

        public fun withChannel(channel: Channel<Lexer>): Builder {
            channels.add(channel)
            return this
        }

        public fun withFailIfNoChannelToConsumeOneCharacter(failIfNoChannelToConsumeOneCharacter: Boolean): Builder {
            this.failIfNoChannelToConsumeOneCharacter = failIfNoChannelToConsumeOneCharacter
            return this
        }

        public val channelDispatcher: ChannelDispatcher<Lexer>
            get() {
                val builder: ChannelDispatcher.Builder = ChannelDispatcher.builder()
                    .addChannels(*channels.toTypedArray())
                if (failIfNoChannelToConsumeOneCharacter) {
                    builder.failIfNoChannelToConsumeOneCharacter()
                }
                return builder.build()
            }
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder {
            return Builder()
        }
    }
}