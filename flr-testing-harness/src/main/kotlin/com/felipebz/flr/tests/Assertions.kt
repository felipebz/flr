//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.api.Rule
import com.felipebz.flr.api.Token
import com.felipebz.flr.channel.Channel
import com.felipebz.flr.channel.CodeReader
import com.felipebz.flr.impl.Parser

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
