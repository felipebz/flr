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
package com.felipebz.flr.profiler

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Grammar
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.api.Token
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.IdentifierAndKeywordChannel
import com.felipebz.flr.parser.ParserAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Pins the documented dispatch of the [Parser] overload families:
 * `parse(file|source)` -> overridable `parse(tokens)`, and
 * `parse(file|source, profiler)` -> overridable `parse(tokens, profiler)` (which does not call `parse(tokens)`).
 */
class ParserSubclassDispatchTest {
    private enum class Rules : GrammarRuleKey { ROOT, WORD }

    private val grammar: Grammar = LexerfulGrammarBuilder.create().also { b ->
        b.rule(Rules.WORD).`is`(GenericTokenType.IDENTIFIER)
        b.rule(Rules.ROOT).`is`(Rules.WORD, GenericTokenType.EOF)
        b.setRootRule(Rules.ROOT)
    }.buildWithMemoizationOfMatchesForAllRules()

    private val lexer: Lexer = Lexer.builder()
        .withFailIfNoChannelToConsumeOneCharacter(true)
        .withChannel(IdentifierAndKeywordChannel("[a-z]++", true))
        .withChannel(BlackHoleChannel("[ \\t\\r\\n]++"))
        .build()

    /**
     * `Parser` gives subclasses no way to supply a lexer (only the builder does), so the inherited lexing
     * path of `parse(source)` / `parse(file)` is exercised by setting the private field reflectively.
     */
    private fun <P : Parser<Grammar>> P.withLexer(): P {
        Parser::class.java.getDeclaredField("lexer").apply { isAccessible = true }.set(this, lexer)
        return this
    }

    /** Overrides only the unprofiled `parse(tokens)`. */
    private inner class OrdinaryOverrideParser : Parser<Grammar>(grammar) {
        val calls = mutableListOf<String>()

        init {
            setRootRule(grammar.rootRule)
        }

        override fun parse(tokens: List<Token>): AstNode {
            calls += "parse(tokens)"
            return super.parse(tokens)
        }
    }

    /** Overrides both token overloads, as the contract asks of a subclass that customizes parsing. */
    private inner class BothOverridesParser : Parser<Grammar>(grammar) {
        val calls = mutableListOf<String>()

        init {
            setRootRule(grammar.rootRule)
        }

        override fun parse(tokens: List<Token>): AstNode {
            calls += "parse(tokens)"
            return super.parse(tokens)
        }

        override fun parse(tokens: List<Token>, profiler: ParsingProfiler): AstNode {
            calls += "parse(tokens, profiler)"
            return super.parse(tokens, profiler)
        }
    }

    private fun AstNode.dump(): String = "$name(${children.joinToString(",") { it.dump() }})"

    private fun ParsingProfiler.rootParses(): Long = snapshot().programs.sumOf { it.parses }

    @Test
    fun unprofiledSourceAndFileParsesStillDispatchThroughParseTokens(@TempDir dir: File) {
        val parser = BothOverridesParser().withLexer()
        val file = File(dir, "input.txt").apply { writeText("one") }

        val fromSource = parser.parse("one")
        val fromFile = parser.parse(file)

        assertThat(parser.calls).containsExactly("parse(tokens)", "parse(tokens)")
        assertThat(fromSource.dump()).isEqualTo(fromFile.dump())
    }

    @Test
    fun profiledSourceAndFileParsesDispatchThroughTheProfiledTokenOverload(@TempDir dir: File) {
        val parser = BothOverridesParser().withLexer()
        val profiler = ParsingProfiler()
        val file = File(dir, "input.txt").apply { writeText("one") }

        val fromSource = parser.parse("one", profiler)
        val fromFile = parser.parse(file, profiler)

        // the profiled family never calls the unprofiled override
        assertThat(parser.calls).containsExactly("parse(tokens, profiler)", "parse(tokens, profiler)")
        assertThat(fromSource.dump()).isEqualTo(parser.parse("one").dump())
        assertThat(fromFile.dump()).isEqualTo(fromSource.dump())
        assertThat(profiler.rootParses()).isEqualTo(2)
    }

    @Test
    fun overridingOnlyTheUnprofiledOverloadIsBypassedByProfiledParsesButStillCollectsCounters() {
        val parser = OrdinaryOverrideParser().withLexer()
        val profiler = ParsingProfiler()

        parser.parse("one", profiler)
        assertThat(parser.calls).isEmpty()
        assertThat(profiler.rootParses()).isEqualTo(1)

        // ...while the unprofiled parse reaches the override and leaves the profiler untouched
        parser.parse("one")
        assertThat(parser.calls).containsExactly("parse(tokens)")
        assertThat(profiler.rootParses()).isEqualTo(1)
    }

    @Test
    fun aSubclassCanInterceptProfiledParsingByOverridingTheProfiledOverload() {
        val intercepted = mutableListOf<ParsingProfiler>()
        val parser = object : Parser<Grammar>(grammar) {
            init {
                setRootRule(grammar.rootRule)
            }

            override fun parse(tokens: List<Token>, profiler: ParsingProfiler): AstNode {
                intercepted += profiler
                return super.parse(tokens, profiler)
            }
        }.withLexer()
        val profiler = ParsingProfiler()

        parser.parse("one", profiler)
        parser.parse("one")

        assertThat(intercepted).containsExactly(profiler)
        assertThat(profiler.rootParses()).isEqualTo(1)
    }

    @Test
    fun unprofiledParsesNeverCollectCounters() {
        val parser = BothOverridesParser().withLexer()
        val profiler = ParsingProfiler()
        parser.parse("one")
        assertThat(profiler.snapshot().programs).isEmpty()
    }

    @Test
    fun exceptionsAreTheSameForProfiledAndUnprofiledParses() {
        val parser = BothOverridesParser().withLexer()
        val profiler = ParsingProfiler()

        val syntax = assertThrows<RecognitionException> { parser.parse("one two") }
        val profiledSyntax = assertThrows<RecognitionException> { parser.parse("one two", profiler) }
        assertThat(profiledSyntax.message).isEqualTo(syntax.message)
        assertThat(profiledSyntax.line).isEqualTo(syntax.line)

        val lexical = assertThrows<RecognitionException> { parser.parse("one 1") }
        val profiledLexical = assertThrows<RecognitionException> { parser.parse("one 1", profiler) }
        assertThat(profiledLexical.message).isEqualTo(lexical.message)

        // only the syntax error reached the VM
        assertThat(profiler.rootParses()).isEqualTo(1)
    }

    @Test
    fun lexerlessParserAdapterParsesCharactersInBothFamiliesAndRejectsTokens() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.WORD).`is`("w")
        b.rule(Rules.ROOT).`is`(Rules.WORD, b.endOfInput())
        b.setRootRule(Rules.ROOT)
        val adapter = ParserAdapter(StandardCharsets.UTF_8, b.build())
        val profiler = ParsingProfiler()

        assertThat(adapter.parse("w", profiler).dump()).isEqualTo(adapter.parse("w").dump())
        assertThat(profiler.rootParses()).isEqualTo(1)
        assertThrows<UnsupportedOperationException> { adapter.parse(emptyList<Token>()) }
        assertThrows<UnsupportedOperationException> { adapter.parse(emptyList<Token>(), profiler) }
    }
}
