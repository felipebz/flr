//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.api.Token
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.internal.matchers.AstCreator
import com.felipebz.flr.internal.matchers.LocatedText
import com.felipebz.flr.profiler.ParsingProfiler
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
        return parse(text, null)
    }

    /**
     * @return constructed AST
     * @throws RecognitionException if unable to parse
     */
    override fun parse(file: File): AstNode {
        val text = LocatedText(file, fileToCharArray(file, charset))
        return parse(text, null)
    }

    /**
     * Same as [parse] with a source, recording counters into [profiler]. Like the unprofiled overload it parses the
     * characters directly through [ParseRunner]; no token-based overload is involved.
     *
     * @since 1.7
     */
    override fun parse(source: String, profiler: ParsingProfiler): AstNode {
        return parse(LocatedText(null, source.toCharArray()), profiler)
    }

    /**
     * Same as [parse] with a file, recording counters into [profiler]. Like the unprofiled overload it parses the
     * characters directly through [ParseRunner]; no token-based overload is involved.
     *
     * @since 1.7
     */
    override fun parse(file: File, profiler: ParsingProfiler): AstNode {
        return parse(LocatedText(file, fileToCharArray(file, charset)), profiler)
    }

    private fun parse(input: LocatedText, profiler: ParsingProfiler?): AstNode {
        val chars = input.toChars()
        val result = if (profiler == null) parseRunner.parse(chars) else parseRunner.parse(chars, profiler)
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

    /**
     * @throws UnsupportedOperationException always: a lexerless grammar parses characters, not tokens
     */
    override fun parse(tokens: List<Token>): AstNode {
        throw UnsupportedOperationException()
    }

    /**
     * @throws UnsupportedOperationException always: a lexerless grammar parses characters, not tokens
     * @since 1.7
     */
    override fun parse(tokens: List<Token>, profiler: ParsingProfiler): AstNode {
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
