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
package com.felipebz.flr.grammar

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Grammar
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.IdentifierAndKeywordChannel
import com.felipebz.flr.impl.channel.PunctuatorChannel
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.regexp
import com.felipebz.flr.parser.ParseRunner
import com.felipebz.flr.parser.ParserAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.charset.Charset

class ContextExpressionTest {
    private val context = ContextKey<Boolean>()

    @Test
    fun lexerless_scopes_nest_isolate_and_restore() {
        val parser = lexerlessScopedParser()

        assertThat(parser.matches("a[1]")).isFalse()
        assertThat(parser.matches("{a[1]}")).isTrue()
        assertThat(parser.matches("{{a[1]}}")).isTrue()
        assertThat(parser.matches("{(a[1])}")).isFalse()
        assertThat(parser.matches("{(a)b[1]}")).isTrue()

        assertThat(parser.matches("a[1]")).isFalse()
    }

    @Test
    fun lexerless_failed_alternatives_restore_context() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(valueExpression(b))
        b.rule(Rules.FAILING).`is`(
            "{",
            b.withContext(context, true, b.oneOrMore(Rules.VALUE)),
            "!",
            "}"
        )
        b.rule(Rules.FALLBACK).`is`("{", Rules.VALUE, "}")
        b.rule(Rules.ROOT).`is`(b.firstOf(Rules.FAILING, Rules.FALLBACK), b.endOfInput())
        b.setRootRule(Rules.ROOT)

        val parser = ParseRunner(b.build().rootRule)
        assertThat(parser.matches("{a[1]}")).isFalse()
    }

    @Test
    fun lexerless_lookahead_restores_context() {
        val positiveLookahead = run {
            val b = LexerlessGrammarBuilder.create()
            b.rule(Rules.VALUE).`is`(valueExpression(b))
            b.rule(Rules.SPECIAL).`is`(b.withContext(context, true, Rules.VALUE))
            b.rule(Rules.ROOT).`is`(b.next(Rules.SPECIAL), Rules.VALUE, b.endOfInput())
            b.setRootRule(Rules.ROOT)
            ParseRunner(b.build().rootRule)
        }
        val negativeLookahead = run {
            val b = LexerlessGrammarBuilder.create()
            b.rule(Rules.VALUE).`is`(valueExpression(b))
            b.rule(Rules.SPECIAL).`is`(b.withContext(context, true, Rules.VALUE), "!")
            b.rule(Rules.ROOT).`is`(b.nextNot(Rules.SPECIAL), Rules.VALUE, b.endOfInput())
            b.setRootRule(Rules.ROOT)
            ParseRunner(b.build().rootRule)
        }

        assertThat(positiveLookahead.matches("a[1]")).isFalse()
        assertThat(negativeLookahead.matches("a[1]")).isFalse()
    }

    @Test
    fun lexerless_exception_does_not_affect_a_later_parse() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(valueExpression(b))
        b.rule(Rules.EXCEPTION).`is`(
            b.withContext(context, true, "{", b.zeroOrMore(b.optional("x")), "}")
        )
        b.rule(Rules.ROOT).`is`(b.firstOf(Rules.EXCEPTION, Rules.VALUE), b.endOfInput())
        b.setRootRule(Rules.ROOT)
        val parser = ParseRunner(b.build().rootRule)

        assertThrows<GrammarException> {
            parser.parse("{a[1]}".toCharArray())
        }
        assertThat(parser.matches("a[1]")).isFalse()
    }

    @Test
    fun lexerless_context_is_not_shared_between_runners() {
        val first = lexerlessScopedParser()
        val second = lexerlessScopedParser()

        assertThat(first.matches("{a[1]}")).isTrue()
        assertThat(second.matches("a[1]")).isFalse()
        assertThat(first.matches("a[1]")).isFalse()
    }

    @Test
    fun context_predicate_can_match_presence_or_a_typed_value() {
        val key = ContextKey<String>()
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.ROOT).`is`(
            b.withContext(
                key,
                "enabled",
                b.requireContext(key),
                b.requireContext(key, "enabled"),
                "x"
            ),
            b.endOfInput()
        )
        b.setRootRule(Rules.ROOT)

        assertThat(ParseRunner(b.build().rootRule).matches("x")).isTrue()
    }

    @Test
    fun nested_context_values_override_and_restore() {
        val key = ContextKey<String>()
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.ROOT).`is`(
            b.withContext(
                key,
                "outer",
                b.requireContext(key, "outer"),
                b.withContext(
                    key,
                    "inner",
                    b.requireContext(key, "inner"),
                    b.nextNot(b.requireContext(key, "outer"))
                ),
                b.requireContext(key, "outer"),
                "x"
            ),
            b.endOfInput()
        )
        b.setRootRule(Rules.ROOT)

        assertThat(ParseRunner(b.build().rootRule).matches("x")).isTrue()
    }

    @Test
    fun false_context_is_distinct_from_absence() {
        val key = ContextKey<Boolean>()
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.ROOT).`is`(
            b.withContext(
                key,
                false,
                b.requireContext(key),
                b.requireContext(key, false),
                b.withoutContext(
                    key,
                    b.nextNot(b.requireContext(key)),
                    b.nextNot(b.requireContext(key, false))
                ),
                b.requireContext(key),
                b.requireContext(key, false),
                "x"
            ),
            b.endOfInput()
        )
        b.setRootRule(Rules.ROOT)

        assertThat(ParseRunner(b.build().rootRule).matches("x")).isTrue()
    }

    @Test
    fun independent_context_keys_do_not_interfere() {
        val first = ContextKey<String>()
        val second = ContextKey<String>()
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.ROOT).`is`(
            b.withContext(
                first,
                "first",
                b.requireContext(first, "first"),
                b.nextNot(b.requireContext(second, "second")),
                b.withContext(
                    second,
                    "second",
                    b.requireContext(first, "first"),
                    b.requireContext(second, "second"),
                    b.withoutContext(
                        second,
                        b.requireContext(first, "first"),
                        b.nextNot(b.requireContext(second, "second"))
                    ),
                    b.requireContext(first, "first"),
                    b.requireContext(second, "second")
                ),
                b.requireContext(first, "first"),
                b.nextNot(b.requireContext(second, "second")),
                "x"
            ),
            b.endOfInput()
        )
        b.setRootRule(Rules.ROOT)

        assertThat(ParseRunner(b.build().rootRule).matches("x")).isTrue()
    }

    @Test
    fun context_keys_use_identity_not_generic_type() {
        val first = ContextKey<String>()
        val second = ContextKey<String>()
        val wrongKeyBuilder = LexerlessGrammarBuilder.create()
        wrongKeyBuilder.rule(Rules.ROOT).`is`(
            wrongKeyBuilder.withContext(
                first,
                "x",
                wrongKeyBuilder.requireContext(second, "x"),
                "x"
            ),
            wrongKeyBuilder.endOfInput()
        )
        wrongKeyBuilder.setRootRule(Rules.ROOT)

        val rightKeyBuilder = LexerlessGrammarBuilder.create()
        rightKeyBuilder.rule(Rules.ROOT).`is`(
            rightKeyBuilder.withContext(
                first,
                "x",
                rightKeyBuilder.requireContext(first, "x"),
                "x"
            ),
            rightKeyBuilder.endOfInput()
        )
        rightKeyBuilder.setRootRule(Rules.ROOT)

        assertThat(ParseRunner(wrongKeyBuilder.build().rootRule).matches("x")).isFalse()
        assertThat(ParseRunner(rightKeyBuilder.build().rootRule).matches("x")).isTrue()
    }

    @Test
    fun lexerless_context_expressions_do_not_add_ast_nodes() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(valueExpression(b))
        b.rule(Rules.ROOT).`is`(b.withContext(context, true, Rules.VALUE), b.endOfInput())
        b.setRootRule(Rules.ROOT)

        val root = ParserAdapter(Charset.defaultCharset(), b.build()).parse("a[1]")

        assertThat(root.numberOfChildren).isEqualTo(1)
        assertThat(root.getFirstChild(Rules.VALUE)).isNotNull()
    }

    @Test
    fun lexerless_memoization_includes_context() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(valueExpression(b))
        b.rule(Rules.ROOT).`is`(
            b.next(Rules.VALUE),
            b.withContext(context, true, Rules.VALUE),
            b.endOfInput()
        )
        b.setRootRule(Rules.ROOT)

        val parser = ParseRunner(b.build().rootRule)

        assertThat(parser.matches("a[1]")).isTrue()
    }

    @Test
    fun lexerless_memoization_distinguishes_context_values() {
        val key = ContextKey<String>()
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(
            b.regexp("[a-z]++"),
            b.optional(
                b.requireContext(key, "A"),
                "[",
                b.regexp("[0-9]++"),
                "]"
            )
        )
        b.rule(Rules.ROOT).`is`(
            b.withContext(key, "A", b.next(Rules.VALUE)),
            b.withContext(key, "B", Rules.VALUE),
            b.endOfInput()
        )
        b.setRootRule(Rules.ROOT)

        assertThat(ParseRunner(b.build().rootRule).matches("a[1]")).isFalse()
    }

    @Test
    fun context_does_not_weaken_left_recursion_detection() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.ROOT).`is`(b.withContext(context, true, Rules.ROOT))
        b.setRootRule(Rules.ROOT)

        val thrown = assertThrows<GrammarException> {
            ParseRunner(b.build().rootRule).parse("".toCharArray())
        }

        assertThat(thrown.message).isEqualTo("Left recursion has been detected, involved rule: ROOT")
    }

    @Test
    fun lexerful_scopes_nest_isolate_and_restore() {
        val parser = lexerfulScopedParser()

        assertThat(parser.matches("a[1]")).isFalse()
        assertThat(parser.matches("{a[1]}")).isTrue()
        assertThat(parser.matches("{{a[1]}}")).isTrue()
        assertThat(parser.matches("{(a[1])}")).isFalse()
        assertThat(parser.matches("{(a)b[1]}")).isTrue()
        assertThat(parser.matches("a[1]")).isFalse()
    }

    @Test
    fun lexerful_failed_alternatives_and_lookahead_restore_context() {
        val failedAlternative = lexerfulParser(memoize = false) { b ->
            b.rule(Rules.FAILING).`is`(
                Tokens.LBRACE,
                b.withContext(context, true, b.oneOrMore(Rules.VALUE)),
                Tokens.BANG,
                Tokens.RBRACE
            )
            b.rule(Rules.FALLBACK).`is`(Tokens.LBRACE, Rules.VALUE, Tokens.RBRACE)
            b.rule(Rules.ROOT).`is`(b.firstOf(Rules.FAILING, Rules.FALLBACK), GenericTokenType.EOF)
        }
        val positiveLookahead = lexerfulParser(memoize = false) { b ->
            b.rule(Rules.SPECIAL).`is`(b.withContext(context, true, Rules.VALUE))
            b.rule(Rules.ROOT).`is`(b.next(Rules.SPECIAL), Rules.VALUE, GenericTokenType.EOF)
        }
        val negativeLookahead = lexerfulParser(memoize = false) { b ->
            b.rule(Rules.SPECIAL).`is`(b.withContext(context, true, Rules.VALUE), Tokens.BANG)
            b.rule(Rules.ROOT).`is`(b.nextNot(Rules.SPECIAL), Rules.VALUE, GenericTokenType.EOF)
        }

        assertThat(failedAlternative.matches("{a[1]}")).isFalse()
        assertThat(positiveLookahead.matches("a[1]")).isFalse()
        assertThat(negativeLookahead.matches("a[1]")).isFalse()
    }

    @Test
    fun lexerful_exception_does_not_affect_a_later_parse() {
        val parser = lexerfulParser(memoize = false) { b ->
            b.rule(Rules.EXCEPTION).`is`(
                b.withContext(context, true, Tokens.LBRACE, b.zeroOrMore(b.optional(Tokens.BANG)), Tokens.RBRACE)
            )
            b.rule(Rules.ROOT).`is`(b.firstOf(Rules.EXCEPTION, Rules.VALUE), GenericTokenType.EOF)
        }

        assertThrows<GrammarException> {
            parser.parse("{a[1]}")
        }
        assertThat(parser.matches("a[1]")).isFalse()
    }

    @Test
    fun lexerful_context_expressions_do_not_add_ast_nodes() {
        val parser = lexerfulParser(
            memoize = false,
            root = { b ->
                b.rule(Rules.ROOT).`is`(
                    b.withContext(context, true, Rules.VALUE),
                    GenericTokenType.EOF
                )
            }
        )

        val root = parser.parse("a[1]")

        assertThat(root.numberOfChildren).isEqualTo(2)
        assertThat(root.getFirstChild(Rules.VALUE)).isNotNull()
    }

    @Test
    fun memoized_lexerful_rules_include_context() {
        val parser = lexerfulParser(memoize = true) { b ->
            b.rule(Rules.ROOT).`is`(
                b.next(Rules.VALUE),
                b.withContext(context, true, Rules.VALUE),
                GenericTokenType.EOF
            )
        }

        assertThat(parser.matches("a[1]")).isTrue()
    }

    @Test
    fun memoized_lexerful_rules_distinguish_context_values() {
        val key = ContextKey<String>()
        val b = LexerfulGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(
            GenericTokenType.IDENTIFIER,
            b.optional(
                b.requireContext(key, "A"),
                Tokens.LBRACKET,
                Tokens.INTEGER,
                Tokens.RBRACKET
            )
        )
        b.rule(Rules.ROOT).`is`(
            b.withContext(key, "A", b.next(Rules.VALUE)),
            b.withContext(key, "B", Rules.VALUE),
            GenericTokenType.EOF
        )
        b.setRootRule(Rules.ROOT)

        val parser = Parser.builder(b.buildWithMemoizationOfMatchesForAllRules())
            .withLexer(lexer())
            .build()

        assertThat(parser.matches("a[1]")).isFalse()
    }

    private fun lexerlessScopedParser(): ParseRunner {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(valueExpression(b))
        b.rule(Rules.ITEM).`is`(b.firstOf(Rules.BLOCK, Rules.ISOLATED, Rules.VALUE))
        b.rule(Rules.BLOCK).`is`(
            b.withContext(context, true, "{", b.oneOrMore(Rules.ITEM), "}")
        )
        b.rule(Rules.ISOLATED).`is`(
            b.withoutContext(context, "(", b.oneOrMore(Rules.ITEM), ")")
        )
        b.rule(Rules.ROOT).`is`(b.firstOf(Rules.BLOCK, Rules.VALUE), b.endOfInput())
        b.setRootRule(Rules.ROOT)
        return ParseRunner(b.build().rootRule)
    }

    private fun valueExpression(b: LexerlessGrammarBuilder): Any {
        return b.sequence(
            b.regexp("[a-z]++"),
            b.optional(contextualSuffix(b))
        )
    }

    private fun contextualSuffix(b: LexerlessGrammarBuilder): Any {
        return b.sequence(
            b.requireContext(context, true),
            "[",
            b.regexp("[0-9]++"),
            "]"
        )
    }

    private fun lexerfulScopedParser(): Parser<Grammar> {
        return lexerfulParser(memoize = false) { b ->
            b.rule(Rules.ITEM).`is`(b.firstOf(Rules.BLOCK, Rules.ISOLATED, Rules.VALUE))
            b.rule(Rules.BLOCK).`is`(
                b.withContext(context, true, Tokens.LBRACE, b.oneOrMore(Rules.ITEM), Tokens.RBRACE)
            )
            b.rule(Rules.ISOLATED).`is`(
                b.withoutContext(context, Tokens.LPAREN, b.oneOrMore(Rules.ITEM), Tokens.RPAREN)
            )
            b.rule(Rules.ROOT).`is`(b.firstOf(Rules.BLOCK, Rules.VALUE), GenericTokenType.EOF)
        }
    }

    private fun lexerfulParser(
        memoize: Boolean,
        root: (LexerfulGrammarBuilder) -> Unit
    ): Parser<Grammar> {
        val b = LexerfulGrammarBuilder.create()
        b.rule(Rules.VALUE).`is`(
            GenericTokenType.IDENTIFIER,
            b.optional(
                b.requireContext(context, true),
                Tokens.LBRACKET,
                Tokens.INTEGER,
                Tokens.RBRACKET
            )
        )
        root(b)
        b.setRootRule(Rules.ROOT)
        val grammar = if (memoize) {
            b.buildWithMemoizationOfMatchesForAllRules()
        } else {
            b.build()
        }
        return Parser.builder(grammar).withLexer(lexer()).build()
    }

    private fun lexer(): Lexer {
        return Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(IdentifierAndKeywordChannel("[a-z]++", true))
            .withChannel(regexp(Tokens.INTEGER, "[0-9]++"))
            .withChannel(
                PunctuatorChannel(
                    Tokens.LBRACE,
                    Tokens.RBRACE,
                    Tokens.LPAREN,
                    Tokens.RPAREN,
                    Tokens.LBRACKET,
                    Tokens.RBRACKET,
                    Tokens.BANG
                )
            )
            .withChannel(BlackHoleChannel("[ \\t\\r\\n]++"))
            .build()
    }

    private fun ParseRunner.matches(source: String): Boolean {
        return parse(source.toCharArray()).isMatched()
    }

    private fun Parser<Grammar>.matches(source: String): Boolean {
        return try {
            parse(source)
            true
        } catch (_: RecognitionException) {
            false
        }
    }

    private enum class Rules : GrammarRuleKey {
        ROOT,
        VALUE,
        ITEM,
        BLOCK,
        ISOLATED,
        FAILING,
        FALLBACK,
        SPECIAL,
        EXCEPTION
    }

    private enum class Tokens(override val value: String) : TokenType {
        INTEGER("INTEGER"),
        LBRACE("{"),
        RBRACE("}"),
        LPAREN("("),
        RPAREN(")"),
        LBRACKET("["),
        RBRACKET("]"),
        BANG("!");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }
}
