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
package com.felipebz.flr.parser

import com.felipebz.flr.api.Rule
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.internal.grammar.MutableParsingRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class LexerlessGrammarTest {
    @Test
    fun should_instanciate_rule_fields() {
        val grammar = TestGrammar()
        assertThat(grammar.rootRule).isInstanceOf(MutableParsingRule::class.java)
        assertThat((grammar.rootRule as MutableParsingRule).getName()).isEqualTo("rule")
    }

    @Test
    fun should_throw_exception() {
        assertThrows<GrammarException>("Unable to instanciate the rule 'rootRule': ") {
            IllegalGrammar()
        }
    }

    private class TestGrammar : LexerlessGrammar() {
        private val rule: Rule? = null
        override val rootRule: Rule
            get() = checkNotNull(rule)
    }

    private class IllegalGrammar : LexerlessGrammar() {
        override val rootRule: Rule
            get() = rule

        companion object {
            private val rule = mock<Rule>()
        }
    }
}
