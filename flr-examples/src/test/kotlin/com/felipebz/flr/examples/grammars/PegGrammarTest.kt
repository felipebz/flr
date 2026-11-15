//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class PegGrammarTest {
    private val g = PegGrammar.create()
    @Test
    fun test() {
        assertThat(g.rule(PegGrammar.WHITESPACE))
            .matches("\n \r \t")
        assertThat(g.rule(PegGrammar.STRING))
            .matches("\"string\"")
        assertThat(g.rule(PegGrammar.RULE_KEY))
            .matches("rule_key")
        assertThat(g.rule(PegGrammar.ATOM))
            .matches("\"string\"")
            .matches("rule_key")
            .matches("( e )")
        assertThat(g.rule(PegGrammar.ZERO_OR_MORE_EXPRESSION))
            .matches("e *")
        assertThat(g.rule(PegGrammar.ONE_OR_MORE_EXPRESSION))
            .matches("e +")
        assertThat(g.rule(PegGrammar.OPTIONAL_EXPRESSION))
            .matches("e ?")
        assertThat(g.rule(PegGrammar.NEXT_NOT_EXPRESSION))
            .matches("! e")
        assertThat(g.rule(PegGrammar.NEXT_EXPRESSION))
            .matches("& e")
        assertThat(g.rule(PegGrammar.SEQUENCE_EXPRESSION))
            .matches("e e")
        assertThat(g.rule(PegGrammar.FIRST_OF_EXPRESSION))
            .matches("e")
            .matches("e | e")
        assertThat(g.rule(PegGrammar.RULE))
            .matches("rule = e ;")
            .matches("rule = e | e ;")
        assertThat(g.rule(PegGrammar.GRAMMAR))
            .matches("rule = e ; rule = e ;")
    }
}
