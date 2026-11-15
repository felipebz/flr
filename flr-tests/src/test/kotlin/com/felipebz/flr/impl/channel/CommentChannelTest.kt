//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.impl.LexerOutput
import com.felipebz.flr.test.testToken
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class CommentChannelTest {
    @Test
    fun testCommentRegexp() {
        val channel = CommentRegexpChannel("//.*")
        val output = LexerOutput()

        assertThat(channel).doesNotConsume("This is not a comment", output)
        assertThat(channel).consume("//My Comment\n second line", output)
        output.addToken(testToken(GenericTokenType.EOF, "EOF"))
        assertThat(output.tokens).hasComment("//My Comment")
        assertThat(output.tokens).hasOriginalComment("//My Comment")
    }
}
