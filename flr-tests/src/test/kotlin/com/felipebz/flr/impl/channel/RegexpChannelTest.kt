//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegexpChannelTest {
    private val output = LexerOutput()
    @Test
    fun testRegexpToHandleNumber() {
        val channel = RegexpChannel(GenericTokenType.CONSTANT, "[0-9]*")
        assertThat(channel).doesNotConsume("Not a number", output)
        assertThat(channel).consume("56;", output)
        assertThat(output.tokens).hasToken("56", GenericTokenType.CONSTANT)
    }

    @Test
    fun testColumnNumber() {
        val channel = RegexpChannel(GenericTokenType.CONSTANT, "[0-9]*")
        assertThat(channel).consume("56;", output)
        assertEquals(output.tokens[0].column, 0)
    }
}
