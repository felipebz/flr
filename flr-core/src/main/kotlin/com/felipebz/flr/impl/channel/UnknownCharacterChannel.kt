//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput

/**
 * Creates token with type [com.felipebz.flr.api.GenericTokenType.UNKNOWN_CHAR] for any character.
 * This channel, if present, should be the last one.
 */
public class UnknownCharacterChannel public constructor() : Channel<LexerOutput> {
    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        if (code.peek() != -1) {
            val unknownChar = output.tokenText().append(code.pop().toChar())
            val token = Token.builder()
                .setType(GenericTokenType.UNKNOWN_CHAR)
                .setValueAndOriginalValue(output.sharedString(unknownChar))
                .setLine(code.getLinePosition())
                .setColumn(code.getColumnPosition() - 1)
                .build()
            output.addToken(token)
            return true
        }
        return false
    }
}
