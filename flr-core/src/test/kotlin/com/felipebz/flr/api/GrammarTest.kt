//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class GrammarTest {

    @Test
    fun method_rule_should_throw_exception_by_default() {
        assertThrows<UnsupportedOperationException> {
            MyGrammar().rule(mock())
        }
    }

    private class MyGrammar : Grammar() {
        override val rootRule: Rule = mock()
    }
}
