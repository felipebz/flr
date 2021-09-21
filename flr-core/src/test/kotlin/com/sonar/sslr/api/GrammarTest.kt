/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package com.sonar.sslr.api

import com.sonar.sslr.api.Grammar.Companion.getAllRuleFields
import com.sonar.sslr.api.Grammar.Companion.getRuleFields
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.parser.LexerlessGrammar
import java.lang.reflect.Field

class GrammarTest {

    @Test
    fun testGetRuleFields() {
        val ruleFields: List<Field> = getRuleFields(
            MyGrammar::class.java
        )
        assertThat(ruleFields.size).isEqualTo(1)
    }

    @Test
    fun testGetAllRuleFields() {
        val ruleFields = getAllRuleFields(
            MyGrammar::class.java
        )
        assertThat(ruleFields.size).isEqualTo(5)
    }

    @Test
    fun method_rule_should_throw_exception_by_default() {
        assertThrows<UnsupportedOperationException> {
            MyGrammar().rule(mock())
        }
    }

    @Test
    fun should_automatically_instanciate_lexerful_rules() {
        val ruleFields = getAllRuleFields(
            MyGrammar::class.java
        )
        val grammar: Grammar = MyGrammar()
        for (ruleField in ruleFields) {
            ruleField.isAccessible = true
            assertThat(ruleField[grammar])
                .`as`("Current rule name = " + ruleField.name).isNotNull.isInstanceOf(
                RuleDefinition::class.java
            )
        }
    }

    @Test
    fun should_automatically_instanciate_lexerless_rules() {
        val ruleFields = getAllRuleFields(
            MyLexerlessGrammar::class.java
        )
        val grammar: LexerlessGrammar = MyLexerlessGrammar()
        for (ruleField in ruleFields) {
            ruleField.isAccessible = true
            assertThat(ruleField[grammar])
                .`as`("Current rule name = " + ruleField.name).isNotNull.isInstanceOf(
                MutableParsingRule::class.java
            )
        }
    }

    @Test
    fun should_throw_exception() {
        assertThrows<GrammarException>("Unable to instanciate the rule 'rootRule': ") {
            IllegalGrammar()
        }
    }

    abstract class MyBaseGrammar : Grammar() {
        var basePackageRule: Rule? = null
        var basePublicRule: Rule? = null
        private val basePrivateRule: Rule? = null
        protected var baseProtectedRule: Rule? = null
    }

    class MyGrammar : MyBaseGrammar() {
        private val junkIntField = 0
        var junkObjectField: Any? = null

        private lateinit var rule: Rule

        override val rootRule: Rule
            get() = rule
    }

    private class MyLexerlessGrammar : LexerlessGrammar() {
        private lateinit var rule: Rule

        override val rootRule: Rule
            get() = rule
    }

    private class IllegalGrammar : Grammar() {
        override val rootRule: Rule
            get() = rule

        companion object {
            private val rule = mock<Rule>()
        }
    }
}