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
package com.felipebz.flr.impl

import com.felipebz.flr.api.*
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.matchers.LexerfulAstCreator
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.internal.vm.CompiledGrammar
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.MutableGrammarCompiler
import com.felipebz.flr.parser.ParserAdapter
import java.io.File

/**
 * To create a new instance of this class use `[Parser.builder]`.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public open class Parser<G : Grammar> {
    public lateinit var rootRule: RuleDefinition
    private val lexer: Lexer?
    private val _grammar: G
    private lateinit var compiledGrammar: CompiledGrammar

    /**
     * @since 1.16
     */
    protected constructor(grammar: G) {
        this._grammar = grammar
        lexer = null
    }

    private constructor(builder: Builder<G>) {
        lexer = builder.lexer
        _grammar = builder.grammar
        rootRule = _grammar.rootRule as RuleDefinition
    }

    public open fun parse(file: File): AstNode {
        checkNotNull(lexer) { "a lexer should be provided" }
        val tokens = try {
            lexer.lex(file)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
        return parse(tokens)
    }

    public open fun parse(source: String): AstNode {
        checkNotNull(lexer) { "a lexer should be provided" }
        val tokens = try {
            lexer.lex(source)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
        return parse(tokens)
    }

    public open fun parse(tokens: List<Token>): AstNode {
        if (::compiledGrammar.isInitialized.not()) {
            compiledGrammar = MutableGrammarCompiler.compile(rootRule as CompilableGrammarRule)
        }
        return LexerfulAstCreator.create(Machine.parse(tokens, compiledGrammar), tokens)
    }

    public val grammar: G
        get() = _grammar

    public fun setRootRule(rootRule: Rule) {
        this.rootRule = rootRule as RuleDefinition
    }

    public class Builder<G : Grammar> {
        private var baseParser: Parser<G>? = null
        public var lexer: Lexer? = null
        public val grammar: G

        public constructor(grammar: G) {
            this.grammar = grammar
        }

        public constructor(parser: Parser<G>) {
            baseParser = parser
            lexer = parser.lexer
            grammar = parser._grammar
        }

        public fun build(): Parser<G> {
            return if (baseParser != null && baseParser is ParserAdapter<*>) {
                baseParser as Parser<G>
            } else Parser(this)
        }

        public fun withLexer(lexer: Lexer?): Builder<G> {
            this.lexer = lexer
            return this
        }
    }

    public companion object {
        @JvmStatic
        public fun <G : Grammar> builder(grammar: G): Builder<G> {
            return Builder(grammar)
        }

        @JvmStatic
        public fun <G : Grammar> builder(parser: Parser<G>): Builder<G> {
            return Builder(parser)
        }
    }
}