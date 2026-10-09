/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
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

import com.felipebz.flr.api.*
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.matchers.LexerfulAstCreator
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.internal.vm.CompiledGrammar
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.MutableGrammarCompiler
import com.felipebz.flr.parser.NonTerminalNodeBuilder
import com.felipebz.flr.parser.ParserAdapter
import com.felipebz.flr.parser.TerminalNodeBuilder
import com.felipebz.flr.profiler.ParsingProfiler
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
    public var nonTerminalNodeBuilder: NonTerminalNodeBuilder =
        NonTerminalNodeBuilder { type: AstNodeType, name: String, token: Token? -> AstNode(type, name, token) }
    public var terminalNodeBuilder: TerminalNodeBuilder =
        TerminalNodeBuilder { token: Token -> AstNode(token) }

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
        nonTerminalNodeBuilder = builder.nonTerminalNodeBuilder ?: nonTerminalNodeBuilder
        terminalNodeBuilder = builder.terminalNodeBuilder ?: terminalNodeBuilder
    }

    public open fun parse(file: File): AstNode {
        return parse(lex(file))
    }

    public open fun parse(source: String): AstNode {
        return parse(lex(source))
    }

    public open fun parse(tokens: List<Token>): AstNode {
        return parseTokens(tokens, null)
    }

    /**
     * Same as [parse], recording rule and memoization counters into [profiler].
     *
     * Lexes like [parse] and continues through the overridable [parse] with `tokens` and [profiler].
     * See that overload for the extension contract.
     *
     * @since 1.7
     */
    public open fun parse(file: File, profiler: ParsingProfiler): AstNode {
        return parse(lex(file), profiler)
    }

    /**
     * Same as [parse], recording rule and memoization counters into [profiler].
     *
     * Lexes like [parse] and continues through the overridable [parse] with `tokens` and [profiler].
     * See that overload for the extension contract.
     *
     * @since 1.7
     */
    public open fun parse(source: String, profiler: ParsingProfiler): AstNode {
        return parse(lex(source), profiler)
    }

    /**
     * Same as [parse], recording rule and memoization counters into [profiler].
     *
     * **Extension contract.** The profiled overloads form their own family and never call the unprofiled ones:
     * `parse(file, profiler)` and `parse(source, profiler)` call this method, which parses directly.
     * It does not call [parse] with `tokens`. A subclass that overrides [parse] with `tokens` (or any other
     * unprofiled overload) to customize parsing must also override the matching profiled overload to get the same
     * customization when profiling; otherwise its override is bypassed by profiled parses. The unprofiled
     * overloads are unchanged: `parse(file)` and `parse(source)` still call the overridable [parse] with `tokens`.
     *
     * @since 1.7
     */
    public open fun parse(tokens: List<Token>, profiler: ParsingProfiler): AstNode {
        return parseTokens(tokens, profiler)
    }

    private fun lex(file: File): List<Token> {
        checkNotNull(lexer) { "a lexer should be provided" }
        return try {
            lexer.lex(file)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
    }

    private fun lex(source: String): List<Token> {
        checkNotNull(lexer) { "a lexer should be provided" }
        return try {
            lexer.lex(source)
        } catch (e: LexerException) {
            throw RecognitionException(e)
        }
    }

    private fun parseTokens(tokens: List<Token>, profiler: ParsingProfiler?): AstNode {
        if (::compiledGrammar.isInitialized.not()) {
            compiledGrammar = MutableGrammarCompiler.compile(rootRule as CompilableGrammarRule)
        }
        val parseTree = Machine.parse(tokens, compiledGrammar, profiler?.countersFor(compiledGrammar))
        return LexerfulAstCreator.create(parseTree, tokens, nonTerminalNodeBuilder, terminalNodeBuilder)
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
        public var nonTerminalNodeBuilder: NonTerminalNodeBuilder? = null
        public var terminalNodeBuilder: TerminalNodeBuilder? = null

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

        public fun withNonTerminalNodeBuilder(nonTerminalNodeBuilder: NonTerminalNodeBuilder): Builder<G> {
            this.nonTerminalNodeBuilder = nonTerminalNodeBuilder
            return this
        }

        public fun withTerminalNodeBuilder(terminalNodeBuilder: TerminalNodeBuilder): Builder<G> {
            this.terminalNodeBuilder = terminalNodeBuilder
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
