/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.test.channel

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.sonar.sslr.channel.Channel
import org.sonar.sslr.channel.CodeReader

class ChannelMatcher<O> : BaseMatcher<Channel<O>> {
    private val sourceCode: String?
    private val output: O?
    private val reader: CodeReader?

    constructor(sourceCode: String, output: O?) {
        this.sourceCode = sourceCode
        this.output = output
        reader = CodeReader(sourceCode)
    }

    constructor(reader: CodeReader, output: O?) {
        this.output = output
        sourceCode = String(reader.peek(30))
        this.reader = reader
    }

    override fun matches(arg0: Any?): Boolean {
        if (arg0 !is Channel<*>) {
            return false
        }
        val channel = arg0 as Channel<O?>
        return channel.consume(checkNotNull(reader), output)
    }

    override fun describeTo(description: Description) {
        description.appendText("Channel consumes '$sourceCode'")
    }
}