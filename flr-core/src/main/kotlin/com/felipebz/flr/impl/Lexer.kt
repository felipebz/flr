//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.ChannelDispatcher
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.channel.CodeReaderConfiguration
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.net.MalformedURLException
import java.nio.charset.Charset

public class Lexer private constructor(builder: Builder) {
    private val charset = builder.charset
    private val configuration = builder.configuration
    private val channelDispatcher = builder.channelDispatcher

    public fun lex(file: File): List<Token> {
        require(file.isFile) { "file \"" + file.absolutePath + "\" must be a file" }
        try {
            file.reader(charset).use { reader ->
                return lex(reader, LexerOutput(file.toURI()))
            }
        } catch (e: MalformedURLException) {
            throw LexerException("Unable to lex file: " + file.absolutePath, e)
        }
    }

    /**
     * Do not use this method, it is intended for internal unit testing only
     *
     * @param sourceCode
     * @return
     */
    public fun lex(sourceCode: String): List<Token> {
        return try {
            lex(StringReader(sourceCode), LexerOutput())
        } catch (e: Exception) {
            throw LexerException("Unable to lex string source code \"$sourceCode\"", e)
        }
    }

    private fun lex(reader: Reader, output: LexerOutput): List<Token> {
        val code = CodeReader(reader, configuration)
        return try {
            channelDispatcher.consume(code, output)
            output.addToken(
                Token.builder()
                    .setType(GenericTokenType.EOF)
                    .setValueAndOriginalValue("EOF")
                    .setLine(code.getLinePosition())
                    .setColumn(code.getColumnPosition())
                    .build()
            )
            output.tokens
        } catch (e: Exception) {
            throw LexerException(
                "Unable to lex source code at line : " + code.getLinePosition() + " and column : "
                        + code.getColumnPosition() + " in file : " + output.uri, e
            )
        }
    }

    public class Builder {
        public var charset: Charset = Charset.defaultCharset()
        public val configuration: CodeReaderConfiguration = CodeReaderConfiguration()
        private val channels = mutableListOf<Channel<LexerOutput>>()
        private var failIfNoChannelToConsumeOneCharacter = false
        public fun build(): Lexer {
            return Lexer(this)
        }

        public fun withCharset(charset: Charset): Builder {
            this.charset = charset
            return this
        }

        public fun withChannel(channel: Channel<LexerOutput>): Builder {
            channels.add(channel)
            return this
        }

        public fun withFailIfNoChannelToConsumeOneCharacter(failIfNoChannelToConsumeOneCharacter: Boolean): Builder {
            this.failIfNoChannelToConsumeOneCharacter = failIfNoChannelToConsumeOneCharacter
            return this
        }

        public val channelDispatcher: ChannelDispatcher<LexerOutput>
            get() {
                val builder: ChannelDispatcher.Builder<LexerOutput> = ChannelDispatcher.builder<LexerOutput>()
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
