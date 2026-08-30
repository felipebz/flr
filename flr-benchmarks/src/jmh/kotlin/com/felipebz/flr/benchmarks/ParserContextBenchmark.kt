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
package com.felipebz.flr.benchmarks

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.internal.matchers.ParseNode
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.internal.vm.CompiledGrammar
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.MutableGrammarCompiler
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(3)
public open class ParserContextBenchmark {
    @JvmField
    @Param("false", "true")
    public var context: Boolean = false

    @JvmField
    @Param("false", "true")
    public var memoized: Boolean = false

    private lateinit var compiled: CompiledGrammar
    private lateinit var inputTokens: List<Token>

    @Setup
    public fun setUp(): Unit {
        val grammar = grammar(context, memoized)
        compiled = MutableGrammarCompiler.compile(grammar.rootRule as CompilableGrammarRule)
        inputTokens = tokens()
    }

    @Benchmark
    public fun parse(): ParseNode {
        return Machine.parse(inputTokens, compiled)
    }

    private fun grammar(context: Boolean, memoized: Boolean): com.felipebz.flr.api.Grammar {
        val b = LexerfulGrammarBuilder.create()
        b.rule(Rules.ATOM).`is`(b.firstOf(Rules.PARENTHESIZED, Tokens.IDENTIFIER, Tokens.NUMBER))
        b.rule(Rules.PARENTHESIZED).`is`(Tokens.LPAREN, Rules.EXPRESSION, Tokens.RPAREN)
        b.rule(Rules.UNARY).`is`(
            b.firstOf(
                b.sequence(Tokens.MINUS, Rules.UNARY),
                b.sequence(Tokens.PLUS, Rules.UNARY),
                Rules.ATOM
            )
        )
        b.rule(Rules.EXPRESSION).`is`(
            Rules.UNARY,
            b.zeroOrMore(b.firstOf(Tokens.PLUS, Tokens.MINUS), Rules.UNARY)
        )
        b.rule(Rules.LATE_FAILURE).`is`(
            Tokens.LET,
            Tokens.IDENTIFIER,
            Tokens.EQUALS,
            Rules.EXPRESSION,
            Tokens.COMMA
        )
        val assignment = b.sequence(
            Tokens.LET,
            Tokens.IDENTIFIER,
            Tokens.EQUALS,
            Rules.EXPRESSION,
            Tokens.SEMICOLON
        )
        val contextualAssignment = if (context) contextualAssignment(b, assignment) else assignment
        b.rule(Rules.STATEMENT).`is`(b.firstOf(Rules.LATE_FAILURE, contextualAssignment))
        b.rule(Rules.ROOT).`is`(
            b.oneOrMore(b.next(Rules.STATEMENT), Rules.STATEMENT),
            GenericTokenType.EOF
        )
        b.setRootRule(Rules.ROOT)
        return if (memoized) {
            b.buildWithMemoizationOfMatchesForAllRules()
        } else {
            b.build()
        }
    }

    /**
     * Reflection keeps the no-context benchmark source-compatible with the
     * pre-context FLR baseline. This setup code is never part of a measurement.
     */
    private fun contextualAssignment(b: LexerfulGrammarBuilder, assignment: Any): Any {
        val contextKeyClass = Class.forName("com.felipebz.flr.grammar.ContextKey")
        val key = contextKeyClass.getConstructor().newInstance()
        val requireContext = b.javaClass.methods.single {
            it.name == "requireContext" && it.parameterCount == 1
        }.invoke(b, key)
        val body = b.sequence(checkNotNull(requireContext), assignment)
        return checkNotNull(
            b.javaClass.methods.single {
                it.name == "withContext" && it.parameterCount == 3
            }.invoke(b, key, true, body)
        )
    }

    private fun tokens(): List<Token> {
        val result = ArrayList<Token>(STATEMENTS * 10 + 1)
        repeat(STATEMENTS) { statement ->
            add(result, Tokens.LET, "let", statement)
            add(result, Tokens.IDENTIFIER, "name", statement)
            add(result, Tokens.EQUALS, "=", statement)
            add(result, Tokens.MINUS, "-", statement)
            add(result, Tokens.LPAREN, "(", statement)
            add(result, Tokens.IDENTIFIER, "value", statement)
            add(result, Tokens.PLUS, "+", statement)
            add(result, Tokens.NUMBER, "1", statement)
            add(result, Tokens.RPAREN, ")", statement)
            add(result, Tokens.SEMICOLON, ";", statement)
        }
        add(result, GenericTokenType.EOF, "", STATEMENTS)
        return result
    }

    private fun add(tokens: MutableList<Token>, type: TokenType, value: String, line: Int) {
        tokens.add(
            Token.builder()
                .setType(type)
                .setValueAndOriginalValue(value)
                .setLine(line + 1)
                .setColumn(0)
                .build()
        )
    }

    private enum class Rules : GrammarRuleKey {
        ROOT,
        STATEMENT,
        LATE_FAILURE,
        EXPRESSION,
        UNARY,
        ATOM,
        PARENTHESIZED
    }

    private enum class Tokens(override val value: String) : TokenType {
        LET("let"),
        IDENTIFIER("identifier"),
        NUMBER("number"),
        EQUALS("="),
        SEMICOLON(";"),
        COMMA(","),
        PLUS("+"),
        MINUS("-"),
        LPAREN("("),
        RPAREN(")");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }

    private companion object {
        const val STATEMENTS: Int = 2_000
    }
}
