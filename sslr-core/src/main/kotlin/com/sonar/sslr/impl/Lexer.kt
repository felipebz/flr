/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package com.sonar.sslr.impl

import com.sonar.sslr.api.*
import org.sonar.sslr.channel.Channel
import org.sonar.sslr.channel.ChannelDispatcher
import org.sonar.sslr.channel.CodeReader
import org.sonar.sslr.channel.CodeReaderConfiguration
import java.io.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class Lexer private constructor(builder: Builder) {
    private val charset: Charset
    private val configuration: CodeReaderConfiguration
    private val channelDispatcher: ChannelDispatcher<Lexer>?
    private val preprocessors: Array<Preprocessor>
    private val trivia: MutableList<Trivia> = LinkedList()
    private var _tokens: MutableList<Token> = ArrayList()

    val tokens: List<Token>
        get() = Collections.unmodifiableList(_tokens)

    var uri: URI? = null
        private set

    fun lex(file: File): List<Token> {
        Objects.requireNonNull(file, "file cannot be null")
        require(file.isFile) { "file \"" + file.absolutePath + "\" must be a file" }
        return try {
            lex(file.toURI().toURL())
        } catch (e: MalformedURLException) {
            throw LexerException("Unable to lex file: " + file.absolutePath, e)
        }
    }

    fun lex(url: URL): List<Token> {
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
    fun lex(sourceCode: String): List<Token> {
        Objects.requireNonNull(sourceCode, "sourceCode cannot be null")
        return try {
            lex(StringReader(sourceCode))
        } catch (e: Exception) {
            throw LexerException("Unable to lex string source code \"$sourceCode\"", e)
        }
    }

    private fun lex(reader: Reader): List<Token> {
        checkNotNull(channelDispatcher) { "the channel dispatcher should be set" }
        _tokens = ArrayList()
        initPreprocessors()
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
            preprocess()
            tokens
        } catch (e: Exception) {
            throw LexerException(
                "Unable to lex source code at line : " + code.getLinePosition() + " and column : "
                        + code.getColumnPosition() + " in file : " + uri, e
            )
        }
    }

    private fun preprocess() {
        for (preprocessor in preprocessors) {
            preprocess(preprocessor)
        }
    }

    private fun preprocess(preprocessor: Preprocessor) {
        val remainingTokens = Collections.unmodifiableList(ArrayList(tokens))
        _tokens.clear()
        var i = 0
        while (i < remainingTokens.size) {
            val action = preprocessor.process(remainingTokens.subList(i, remainingTokens.size))
            checkNotNull(action) { "A preprocessor cannot return a null PreprocessorAction" }
            addTrivia(action.getTriviaToInject())
            for (j in 0 until action.getNumberOfConsumedTokens()) {
                val removedToken = remainingTokens[i]
                i++
                addTrivia(removedToken.trivia)
            }
            for (tokenToInject in action.getTokensToInject()) {
                addToken(tokenToInject)
            }
            if (action.getNumberOfConsumedTokens() == 0) {
                val removedToken = remainingTokens[i]
                i++
                addTrivia(removedToken.trivia)
                addToken(removedToken)
            }
        }
    }

    private fun initPreprocessors() {
        for (preprocessor in preprocessors) {
            preprocessor.init()
        }
    }

    fun addTrivia(vararg trivia: Trivia) {
        addTrivia(listOf(*trivia))
    }

    fun addTrivia(trivia: List<Trivia>) {
        this.trivia.addAll(trivia)
    }

    fun addToken(vararg tokens: Token) {
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

    class Builder {
        var charset: Charset = Charset.defaultCharset()
        val preprocessors: MutableList<Preprocessor> = ArrayList()
        val configuration = CodeReaderConfiguration()
        private val channels: MutableList<Channel<Lexer>> = ArrayList()
        private var failIfNoChannelToConsumeOneCharacter = false
        fun build(): Lexer {
            return Lexer(this)
        }

        fun withCharset(charset: Charset): Builder {
            this.charset = charset
            return this
        }

        @Deprecated("in 1.20 - use your own preprocessor instead")
        fun withPreprocessor(preprocessor: Preprocessor): Builder {
            preprocessors.add(preprocessor)
            return this
        }

        fun withChannel(channel: Channel<Lexer>): Builder {
            channels.add(channel)
            return this
        }

        fun withFailIfNoChannelToConsumeOneCharacter(failIfNoChannelToConsumeOneCharacter: Boolean): Builder {
            this.failIfNoChannelToConsumeOneCharacter = failIfNoChannelToConsumeOneCharacter
            return this
        }

        val channelDispatcher: ChannelDispatcher<Lexer>
            get() {
                val builder: ChannelDispatcher.Builder = ChannelDispatcher.builder()
                    .addChannels(*channels.toTypedArray())
                if (failIfNoChannelToConsumeOneCharacter) {
                    builder.failIfNoChannelToConsumeOneCharacter()
                }
                return builder.build()
            }
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    init {
        charset = builder.charset
        preprocessors = builder.preprocessors.toTypedArray()
        configuration = builder.configuration
        channelDispatcher = builder.channelDispatcher
        try {
            uri = URI("tests://unittest")
        } catch (e: URISyntaxException) {
            // Can't happen
            throw IllegalStateException(e)
        }
    }
}