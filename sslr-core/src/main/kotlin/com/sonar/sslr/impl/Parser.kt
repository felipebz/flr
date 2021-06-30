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
package com.sonar.sslr.impl

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.sonar.sslr.internal.matchers.LexerfulAstCreator
import org.sonar.sslr.internal.vm.CompilableGrammarRule
import org.sonar.sslr.internal.vm.CompiledGrammar
import org.sonar.sslr.internal.vm.Machine
import org.sonar.sslr.internal.vm.MutableGrammarCompiler
import org.sonar.sslr.parser.*
import java.io.File

/**
 * To create a new instance of this class use `[Parser.builder]`.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
open class Parser<G : Grammar> {
    private var rootRule: RuleDefinition? = null
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
        rootRule = _grammar.getRootRule() as RuleDefinition
    }

    open fun parse(file: File): AstNode {
        checkNotNull(lexer) { "a lexer should be provided" }
        try {
            lexer.lex(file)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
        return parse(lexer.tokens)
    }

    open fun parse(source: String): AstNode {
        checkNotNull(lexer) { "a lexer should be provided" }
        try {
            lexer.lex(source)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
        return parse(lexer.tokens)
    }

    open fun parse(tokens: List<Token>): AstNode {
        // TODO can be compiled only once
        val g: CompiledGrammar = MutableGrammarCompiler.compile(rootRule as CompilableGrammarRule)
        return LexerfulAstCreator.create(Machine.parse(tokens, g), tokens)
    }

    val grammar: G
        get() = _grammar

    open fun getRootRule(): RuleDefinition? {
        return rootRule
    }

    fun setRootRule(rootRule: Rule?) {
        this.rootRule = rootRule as RuleDefinition?
    }

    class Builder<G : Grammar> {
        private var baseParser: Parser<G>? = null
        var lexer: Lexer? = null
        val grammar: G

        constructor(grammar: G) {
            this.grammar = grammar
        }

        constructor(parser: Parser<G>) {
            baseParser = parser
            lexer = parser.lexer
            grammar = parser._grammar
        }

        fun build(): Parser<G> {
            return if (baseParser != null && baseParser is ParserAdapter<*>) {
                baseParser as Parser<G>
            } else Parser(this)
        }

        fun withLexer(lexer: Lexer?): Builder<G> {
            this.lexer = lexer
            return this
        }
    }

    companion object {
        @JvmStatic
        fun <G : Grammar> builder(grammar: G): Builder<G> {
            return Builder(grammar)
        }

        @JvmStatic
        fun <G : Grammar> builder(parser: Parser<G>): Builder<G> {
            return Builder(parser)
        }
    }
}