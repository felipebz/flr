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
package com.felipebz.flr.impl

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.CommentRegexpChannel
import com.felipebz.flr.impl.channel.RegexpChannel
import com.felipebz.flr.impl.channel.UnknownCharacterChannel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LexerOutputTest {
    private val output = LexerOutput()

    private fun token(value: String): Token =
        Token.builder()
            .setType(GenericTokenType.IDENTIFIER)
            .setValueAndOriginalValue(value)
            .setLine(1)
            .setColumn(0)
            .build()

    @Test
    fun tokenCountReflectsEmittedTokens() {
        assertEquals(0, output.tokenCount)
        output.addToken(token("a"))
        assertEquals(1, output.tokenCount)
        output.addToken(token("b"), token("c"))
        assertEquals(3, output.tokenCount)
    }

    @Test
    fun lastTokenIsNullWhenEmptyAndLatestOtherwise() {
        assertNull(output.lastToken)
        output.addToken(token("a"))
        assertSame(output.tokenAtOrNull(0), output.lastToken)
        output.addToken(token("b"), token("c"))
        assertEquals("c", output.lastToken?.value)
    }

    @Test
    fun tokenAtOrNullHandlesBoundsAndReturnsStoredToken() {
        assertNull(output.tokenAtOrNull(0))
        output.addToken(token("a"), token("b"))
        assertNull(output.tokenAtOrNull(-1))
        assertNull(output.tokenAtOrNull(2))
        assertSame(output.tokens[0], output.tokenAtOrNull(0))
        assertSame(output.tokens[1], output.tokenAtOrNull(1))
    }

    @Test
    fun tokensRemainsASnapshot() {
        output.addToken(token("a"))
        val snapshot = output.tokens
        output.addToken(token("b"))

        assertEquals(listOf("a"), snapshot.map { it.value })
        assertEquals(listOf("a", "b"), output.tokens.map { it.value })
    }

    @Test
    fun sharedStringGivesOneStringPerText() {
        val first = output.sharedString(StringBuilder("abc"))
        assertEquals("abc", first)
        assertSame(first, output.sharedString(StringBuilder("abc")))
        assertSame(first, output.sharedString("abc"))
        assertEquals("", output.sharedString(StringBuilder()))
    }

    @Test
    fun tokenTextIsFoundLikeAnEqualText() {
        val fromString = output.sharedString("abc")
        val text = output.tokenText().append('a').append("bc")
        assertEquals("abc", text.toString())
        assertEquals('b', text[1])
        assertSame(fromString, output.sharedString(text))

        assertEquals(0, output.tokenText().length)
        val fromText = output.sharedString(output.tokenText().append("xyz", 1, 3))
        assertEquals("yz", fromText)
        assertSame(fromText, output.sharedString(StringBuilder("yz")))
        assertEquals("null", output.tokenText().append(null).toString())
    }

    @Test
    fun sharedStringKeepsTextsWithEqualHashesApart() {
        // "Aa" and "BB" have the same hash code.
        val aa = output.sharedString(output.tokenText().append("Aa"))
        val bb = output.sharedString(output.tokenText().append("BB"))
        assertEquals("Aa", aa)
        assertEquals("BB", bb)
        assertSame(aa, output.sharedString("Aa"))
        assertSame(bb, output.sharedString(StringBuilder("BB")))
    }

    @Test
    fun sharedStringFindsEveryTextAfterManyTexts() {
        val strings = List(5000) { output.sharedString(output.tokenText().append("text$it")) }
        for (i in strings.indices) {
            val again = output.sharedString(output.tokenText().append("text$i"))
            assertEquals("text$i", again)
            assertSame(strings[i], again)
        }
    }

    @Test
    fun sharedResultIsComputedOncePerTextAndKey() {
        val calls = mutableListOf<String>()
        val upper: LexerOutput.(String) -> String = { calls.add(it); sharedString(it.uppercase()) }
        val first = output.sharedResult(output.tokenText().append("ab"), "upper", upper)
        assertEquals("AB", first)
        assertSame(first, output.sharedResult(StringBuilder("ab"), "upper", upper))
        assertSame(first, output.sharedString("AB"))
        assertEquals(listOf("ab"), calls)
        assertSame(output.sharedString("ab"), calls.single())

        assertEquals("ab!", output.sharedResult("ab", "other") { "$it!" })
        assertEquals("AB", output.sharedResult("ab", "upper", upper))
        assertEquals(2, calls.size)
    }

    @Test
    fun sharedResultSurvivesStringsAddedWhileComputing() {
        val result = output.sharedResult("word", "key") { word ->
            repeat(5000) { sharedString("other$it") }
            word.length
        }
        assertEquals(4, result)
        assertEquals(4, output.sharedResult<Int>(output.tokenText().append("word"), "key") { error("computed again") })
    }

    @Test
    fun eachOutputHasItsOwnStrings() {
        assertNotSame(output.sharedString(StringBuilder("abc")), LexerOutput().sharedString(StringBuilder("abc")))
    }

    @Test
    fun tokensAndCommentsOfOneLexShareEqualStrings() {
        val lexer = Lexer.builder()
            .withChannel(BlackHoleChannel("\\s++"))
            .withChannel(CommentRegexpChannel("//[^\\n]*+"))
            .withChannel(RegexpChannel(GenericTokenType.IDENTIFIER, "[a-z]++"))
            .withChannel(UnknownCharacterChannel())
            .build()
        val tokens = lexer.lex("ab // c\n! ab // c\n!")

        assertEquals(listOf("ab", "!", "ab", "!", "EOF"), tokens.map { it.value })
        assertSame(tokens[0].value, tokens[2].value)
        assertSame(tokens[0].originalValue, tokens[2].originalValue)
        assertSame(tokens[1].value, tokens[3].value)
        assertSame(tokens[1].trivia.single().token.value, tokens[3].trivia.single().token.value)
        assertNotSame(tokens[0].value, lexer.lex("ab")[0].value)
    }
}
