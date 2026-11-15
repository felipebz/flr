//
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import org.assertj.core.api.AbstractAssert

public class ChannelAssert<O>(actual: Channel<O>?) : AbstractAssert<ChannelAssert<O>, Channel<O>>(
    actual, ChannelAssert::class.java
) {

    private fun checkIfConsume(sourceCode: String, output: O): Boolean {
        return checkIfConsume(CodeReader(sourceCode), output)
    }

    private fun checkIfConsume(reader: CodeReader, output: O): Boolean {
        return actual.consume(reader, output)
    }

    public fun consume(sourceCode: String, output: O): ChannelAssert<O> {
        isNotNull
        if (!checkIfConsume(sourceCode, output)) {
            throw failure("Channel did not consume '$sourceCode'")
        }
        return this
    }

    public fun consume(reader: CodeReader, output: O): ChannelAssert<O> {
        isNotNull
        if (!checkIfConsume(reader, output)) {
            throw failure("Channel did not consume '$reader'")
        }
        return this
    }

    public fun doesNotConsume(sourceCode: String, output: O): ChannelAssert<O> {
        isNotNull
        if (checkIfConsume(sourceCode, output)) {
            throw failure("Channel did consume '$sourceCode' but it should not")
        }
        return this
    }

    public fun doesNotConsume(reader: CodeReader, output: O): ChannelAssert<O> {
        isNotNull
        if (checkIfConsume(reader, output)) {
            throw failure("Channel did consume '$reader' but it should not")
        }
        return this
    }

}
