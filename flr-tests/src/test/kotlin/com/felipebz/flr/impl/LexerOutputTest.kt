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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
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
}
