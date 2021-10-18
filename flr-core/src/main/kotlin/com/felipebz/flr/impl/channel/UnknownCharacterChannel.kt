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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput

/**
 * Creates token with type [com.felipebz.flr.api.GenericTokenType.UNKNOWN_CHAR] for any character.
 * This channel, if present, should be the last one.
 *
 * @since 1.2
 */
public class UnknownCharacterChannel public constructor() : Channel<LexerOutput> {
    private val tokenBuilder: Token.Builder = Token.builder()

    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        if (code.peek() != -1) {
            val unknownChar = code.pop().toChar()
            val token = tokenBuilder
                .setType(GenericTokenType.UNKNOWN_CHAR)
                .setValueAndOriginalValue(unknownChar.toString())
                .setLine(code.getLinePosition())
                .setColumn(code.getColumnPosition() - 1)
                .build()
            output.addToken(token)
            return true
        }
        return false
    }
}