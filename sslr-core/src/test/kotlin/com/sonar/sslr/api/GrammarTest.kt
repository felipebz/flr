/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package com.sonar.sslr.api

import com.sonar.sslr.api.Grammar.Companion.getAllRuleFields
import com.sonar.sslr.api.Grammar.Companion.getRuleFields
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.parser.LexerlessGrammar
import java.lang.reflect.Field

class GrammarTest {

    @Test
    fun testGetRuleFields() {
        val ruleFields: List<Field> = getRuleFields(
            MyGrammar::class.java
        )
        Assertions.assertThat(ruleFields.size).isEqualTo(1)
    }

    @Test
    fun testGetAllRuleFields() {
        val ruleFields = getAllRuleFields(
            MyGrammar::class.java
        )
        Assertions.assertThat(ruleFields.size).isEqualTo(5)
    }

    @Test
    fun method_rule_should_throw_exception_by_default() {
        assertThrows(UnsupportedOperationException::class.java) {
            MyGrammar().rule(Mockito.mock(GrammarRuleKey::class.java))
        }
    }

    @Test
    @Throws(IllegalAccessException::class)
    fun should_automatically_instanciate_lexerful_rules() {
        val ruleFields = getAllRuleFields(
            MyGrammar::class.java
        )
        val grammar: Grammar = MyGrammar()
        for (ruleField in ruleFields) {
            ruleField.isAccessible = true
            Assertions.assertThat(ruleField[grammar])
                .`as`("Current rule name = " + ruleField.name).isNotNull.isInstanceOf(
                RuleDefinition::class.java
            )
        }
    }

    @Test
    @Throws(IllegalAccessException::class)
    fun should_automatically_instanciate_lexerless_rules() {
        val ruleFields = getAllRuleFields(
            MyLexerlessGrammar::class.java
        )
        val grammar: LexerlessGrammar = MyLexerlessGrammar()
        for (ruleField in ruleFields) {
            ruleField.isAccessible = true
            Assertions.assertThat(ruleField[grammar])
                .`as`("Current rule name = " + ruleField.name).isNotNull.isInstanceOf(
                MutableParsingRule::class.java
            )
        }
    }

    @Test
    fun should_throw_exception() {
        assertThrows("Unable to instanciate the rule 'rootRule': ", GrammarException::class.java) {
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

        @JvmField
        var rootRule: Rule? = null

        override fun getRootRule(): Rule? {
            return rootRule
        }
    }

    private class MyLexerlessGrammar : LexerlessGrammar() {
        @JvmField
        var rootRule: Rule? = null

        override fun getRootRule(): Rule? {
            return rootRule
        }
    }

    private class IllegalGrammar : Grammar() {
        override fun getRootRule(): Rule? {
            return rootRule
        }

        companion object {
            private val rootRule = Mockito.mock(Rule::class.java)
        }
    }
}