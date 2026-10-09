/**
 * FLR
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
import com.felipebz.flr.api.RecognitionException
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
import java.nio.charset.StandardCharsets

class ProfiledParserApiTest {
    private enum class Rules : GrammarRuleKey { ROOT, WORD, PAIR }

    private fun lexerfulParser(): Parser<com.felipebz.flr.api.Grammar> {
        val b = LexerfulGrammarBuilder.create()
        b.rule(Rules.WORD).`is`(GenericTokenType.IDENTIFIER)
        b.rule(Rules.PAIR).`is`(Rules.WORD, Rules.WORD)
        b.rule(Rules.ROOT).`is`(b.firstOf(b.sequence(Rules.PAIR, GenericTokenType.EOF), b.sequence(Rules.WORD, GenericTokenType.EOF)))
        b.setRootRule(Rules.ROOT)
        val lexer = Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(IdentifierAndKeywordChannel("[a-z]++", true))
            .withChannel(BlackHoleChannel("[ \\t\\r\\n]++"))
            .build()
        return Parser.builder(b.buildWithMemoizationOfMatchesForAllRules()).withLexer(lexer).build()
    }

    private fun AstNode.dump(): String = "$name(${children.joinToString(",") { it.dump() }})"

    @Test
    fun lexerfulProfiledParseMatchesTheOrdinaryParseAndRecordsCounters() {
        val parser = lexerfulParser()
        val profiler = ParsingProfiler()

        assertThat(parser.parse("one", profiler).dump()).isEqualTo(parser.parse("one").dump())
        assertThat(parser.parse("one two", profiler).dump()).isEqualTo(parser.parse("one two").dump())

        val program = profiler.snapshot().programs.single()
        assertThat(program.parses).isEqualTo(2)
        assertThat(program.root.name).isEqualTo("ROOT")
        assertThat(program.root.matches).isEqualTo(2)
        // "one": PAIR fails after its first WORD, ROOT's second alternative reuses WORD's memo
        val word = program.targets.single { it.name == "WORD" }
        assertThat(word.memoHits).isGreaterThanOrEqualTo(1)
        assertThat(word.memoLookups).isEqualTo(word.memoHits + word.memoEmptyMisses + word.memoMatcherMisses + word.memoContextMisses)
        assertThat(program.targets.single { it.name == "PAIR" }.failures).isEqualTo(1)
    }

    @Test
    fun ordinaryParsesDoNotTouchTheProfiler() {
        val parser = lexerfulParser()
        val profiler = ParsingProfiler()
        parser.parse("one")
        parser.parse("one two")
        assertThat(profiler.snapshot().programs).isEmpty()
    }

    @Test
    fun failedProfiledParsesStillThrowAndAreCounted() {
        val parser = lexerfulParser()
        val profiler = ParsingProfiler()
        val ordinary = assertThrows<RecognitionException> { parser.parse("one two three") }
        val profiled = assertThrows<RecognitionException> { parser.parse("one two three", profiler) }
        assertThat(profiled.message).isEqualTo(ordinary.message)
        val root = profiler.snapshot().programs.single().root
        assertThat(root.executedInvocations).isEqualTo(1)
        assertThat(root.failures).isEqualTo(1)
    }

    @Test
    fun lexerlessParserAdapterProfilesThroughParseRunner() {
        val b = LexerlessGrammarBuilder.create()
        b.rule(Rules.WORD).`is`("w")
        b.rule(Rules.ROOT).`is`(b.firstOf(b.sequence(Rules.WORD, "x"), b.sequence(Rules.WORD, "y")), b.endOfInput())
        b.setRootRule(Rules.ROOT)
        val parser = ParserAdapter(StandardCharsets.UTF_8, b.build())
        val profiler = ParsingProfiler()

        assertThat(parser.parse("wy", profiler).dump()).isEqualTo(parser.parse("wy").dump())

        val program = profiler.snapshot().programs.single()
        val word = program.targets.single { it.name == "WORD" }
        // lexerless rules always memoize
        assertThat(word.memoizing).isTrue()
        assertThat(word.memoHits).isEqualTo(1)
        assertThat(word.memoEmptyMisses).isEqualTo(1)
        assertThat(word.matches).isEqualTo(1)
        assertThat(word.memoStores).isEqualTo(1)
    }
}
