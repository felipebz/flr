//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput

/**
 * Ignores all BOM characters.
 */
public class BomCharacterChannel : Channel<LexerOutput> {
    override fun consume(code: CodeReader, output: LexerOutput): Boolean {
        if (code.peek() == BOM_CHAR) {
            code.pop()
            return true
        }
        return false
    }

    public companion object {
        public const val BOM_CHAR: Int = '\uFEFF'.code
    }
}
