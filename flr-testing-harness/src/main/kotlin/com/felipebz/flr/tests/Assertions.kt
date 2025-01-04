/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
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

import com.felipebz.flr.api.Rule
import com.felipebz.flr.api.Token
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.Parser

/**
 * @since 1.16
 */
public object Assertions {
    /**
     * Creates a new instance of `[RuleAssert]`.
     * @param actual the value to be the target of the assertions methods.
     * @return the created assertion object.
     */
    public fun assertThat(actual: Rule?): RuleAssert {
        return RuleAssert(actual)
    }

    /**
     * Creates a new instance of `[ParserAssert]`.
     * @param actual the value to be the target of the assertions methods.
     * @return the created assertion object.
     */
    @JvmStatic
    public fun assertThat(actual: Parser<*>): ParserAssert {
        return ParserAssert(actual)
    }

    /**
     * Creates a new instance of `[ChannelAssert]`.
     * @param actual the value to be the target of the assertions methods.
     * @return the created assertion object.
     */
    @JvmStatic
    public fun <O> assertThat(actual: Channel<O>): ChannelAssert<O> {
        return ChannelAssert<O>(actual)
    }

    /**
     * Creates a new instance of `[TokenListAssert]`.
     * @param actual the value to be the target of the assertions methods.
     * @return the created assertion object.
     */
    @JvmStatic
    public fun assertThat(actual: List<Token>): TokenListAssert {
        return TokenListAssert(actual)
    }

    /**
     * Creates a new instance of `[CodeReaderAssert]`.
     * @param actual the value to be the target of the assertions methods.
     * @return the created assertion object.
     */
    @JvmStatic
    public fun assertThat(actual: CodeReader): CodeReaderAssert {
        return CodeReaderAssert(actual)
    }
}
