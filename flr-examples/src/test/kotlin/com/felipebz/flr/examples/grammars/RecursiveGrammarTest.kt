//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class RecursiveGrammarTest {
    private val grammar = RecursiveGrammar.create()
    @Test
    fun test() {
        val sb = StringBuilder()
        for (i in 0..99999) {
            sb.append('(')
        }
        for (i in 0..99999) {
            sb.append(')')
        }
        assertThat(grammar.rule(RecursiveGrammar.S)).matches(sb.toString())
    }
}
