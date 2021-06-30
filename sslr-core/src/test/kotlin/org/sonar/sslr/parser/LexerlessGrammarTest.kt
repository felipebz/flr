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
package org.sonar.sslr.parser

import com.sonar.sslr.api.Rule
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.grammar.MutableParsingRule

class LexerlessGrammarTest {
    @Test
    fun should_instanciate_rule_fields() {
        val grammar = TestGrammar()
        Assertions.assertThat(grammar.getRootRule()).isInstanceOf(MutableParsingRule::class.java)
        Assertions.assertThat((grammar.getRootRule() as MutableParsingRule).getName()).isEqualTo("rootRule")
    }

    @Test
    fun should_throw_exception() {
        assertThrows("Unable to instanciate the rule 'rootRule': ", GrammarException::class.java) {
            IllegalGrammar()
        }
    }

    private class TestGrammar : LexerlessGrammar() {
        private val rootRule: Rule? = null
        override fun getRootRule(): Rule {
            return checkNotNull(rootRule)
        }
    }

    private class IllegalGrammar : LexerlessGrammar() {
        override fun getRootRule(): Rule {
            return checkNotNull(rootRule)
        }

        companion object {
            private val rootRule = Mockito.mock(Rule::class.java)
        }
    }
}