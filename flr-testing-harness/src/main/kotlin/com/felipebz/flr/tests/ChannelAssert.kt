/**
 * FLR
 * Copyright (C) 2021-2025 Felipe Zorzo
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
