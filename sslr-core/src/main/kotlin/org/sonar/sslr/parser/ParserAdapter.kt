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
package org.sonar.sslr.parser

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.Parser
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.matchers.AstCreator
import org.sonar.sslr.internal.matchers.LocatedText
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Adapts [ParseRunner] to be used as [Parser].
 *
 *
 * This class is not intended to be subclassed by clients.
 *
 * @since 1.16
 */
class ParserAdapter<G : LexerlessGrammar>(charset: Charset, grammar: G) :
    Parser<G>(Objects.requireNonNull(grammar, "grammar")) {
    private val charset: Charset = Objects.requireNonNull(charset, "charset")
    private val parseRunner: ParseRunner = ParseRunner(checkNotNull(grammar.getRootRule()))

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
            val line = inputBuffer.getPosition(parseError.getErrorIndex()).getLine()
            val message = ParseErrorFormatter().format(parseError)
            throw RecognitionException(line, message)
        }
    }

    override fun parse(tokens: List<Token>): AstNode {
        throw UnsupportedOperationException()
    }

    override fun getRootRule(): RuleDefinition? {
        throw UnsupportedOperationException()
    }

    companion object {
        private fun fileToCharArray(file: File, charset: Charset): CharArray {
            return try {
                String(Files.readAllBytes(Paths.get(file.path)), charset).toCharArray()
            } catch (e: IOException) {
                throw RecognitionException(0, e.message, e)
            }
        }
    }

}