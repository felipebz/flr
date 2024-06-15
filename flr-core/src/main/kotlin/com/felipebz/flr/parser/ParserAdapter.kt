/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.parser

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.api.Token
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.internal.matchers.AstCreator
import com.felipebz.flr.internal.matchers.LocatedText
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Adapts [ParseRunner] to be used as [Parser].
 *
 *
 * This class is not intended to be subclassed by clients.
 *
 * @since 1.16
 */
public class ParserAdapter<G : LexerlessGrammar>(private val charset: Charset, grammar: G) :
    Parser<G>(grammar) {
    private val parseRunner: ParseRunner = ParseRunner(grammar.rootRule)

    /**
     * @return constructed AST
     * @throws RecognitionException if unable to parse
     */
    override fun parse(source: String): AstNode {
        // LocatedText is used in order to be able to retrieve TextLocation
        val text = LocatedText(null, source.toCharArray())
        return parse(text)
    }

    /**
     * @return constructed AST
     * @throws RecognitionException if unable to parse
     */
    override fun parse(file: File): AstNode {
        val text = LocatedText(file, fileToCharArray(file, charset))
        return parse(text)
    }

    private fun parse(input: LocatedText): AstNode {
        val chars = input.toChars()
        val result = parseRunner.parse(chars)
        return if (result.isMatched()) {
            AstCreator.create(result, input)
        } else {
            val parseError = checkNotNull(result.getParseError())
            val inputBuffer = parseError.getInputBuffer()
            val position = inputBuffer.getPosition(parseError.getErrorIndex())
            val line = position.getLine()
            val column = position.getColumn()
            val message = ParseErrorFormatter().format(parseError)
            throw RecognitionException(line, column, message)
        }
    }

    override fun parse(tokens: List<Token>): AstNode {
        throw UnsupportedOperationException()
    }

    public companion object {
        private fun fileToCharArray(file: File, charset: Charset): CharArray {
            return try {
                String(Files.readAllBytes(Paths.get(file.path)), charset).toCharArray()
            } catch (e: IOException) {
                throw RecognitionException(0, 0, e.message ?: "Unable to read file", e)
            }
        }
    }

}