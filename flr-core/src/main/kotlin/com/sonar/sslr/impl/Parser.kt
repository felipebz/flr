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
package com.sonar.sslr.impl

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.matchers.LexerfulAstCreator
import org.sonar.sslr.internal.vm.CompilableGrammarRule
import org.sonar.sslr.internal.vm.CompiledGrammar
import org.sonar.sslr.internal.vm.Machine
import org.sonar.sslr.internal.vm.MutableGrammarCompiler
import org.sonar.sslr.parser.ParserAdapter
import java.io.File

/**
 * To create a new instance of this class use `[Parser.builder]`.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public open class Parser<G : Grammar> {
    private lateinit var rootRule: RuleDefinition
    private val lexer: Lexer?
    private val _grammar: G

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
        try {
            lexer.lex(file)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
        return parse(lexer.tokens)
    }

    public open fun parse(source: String): AstNode {
        checkNotNull(lexer) { "a lexer should be provided" }
        try {
            lexer.lex(source)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
        return parse(lexer.tokens)
    }

    public open fun parse(tokens: List<Token>): AstNode {
        // TODO can be compiled only once
        val g: CompiledGrammar = MutableGrammarCompiler.compile(rootRule as CompilableGrammarRule)
        return LexerfulAstCreator.create(Machine.parse(tokens, g), tokens)
    }

    public val grammar: G
        get() = _grammar

    public open fun getRootRule(): RuleDefinition {
        return rootRule
    }

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