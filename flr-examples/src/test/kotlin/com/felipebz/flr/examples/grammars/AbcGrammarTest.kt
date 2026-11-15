//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.examples.grammars.AbcGrammar.Companion.createGrammar
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class AbcGrammarTest {
    private val g = createGrammar()
    @Test
    fun test() {
        assertThat(g.rule(AbcGrammar.S))
            .matches("abc")
            .matches("aabbcc")
            .matches("aaabbbccc")
            .notMatches("aabc")
            .notMatches("aabbc")
            .notMatches("aabcc")
            .notMatches("abbc")
            .notMatches("abbcc")
            .notMatches("abcc")
    }
}
