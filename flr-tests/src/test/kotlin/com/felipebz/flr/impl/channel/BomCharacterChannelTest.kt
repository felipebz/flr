//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.LexerOutput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BomCharacterChannelTest {
    private val output = LexerOutput()
    private val channel = BomCharacterChannel()
    @Test
    fun shouldConsumeBomCharacter() {
        assertThat(channel.consume(CodeReader("\uFEFF"), output)).isTrue()
        assertThat(output.tokens.size).isEqualTo(0)
    }

    @Test
    fun shouldNotConsumeOtherCharacters() {
        assertThat(channel.consume(CodeReader(" "), output)).isFalse()
        assertThat(output.tokens.size).isEqualTo(0)
    }
}
