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
