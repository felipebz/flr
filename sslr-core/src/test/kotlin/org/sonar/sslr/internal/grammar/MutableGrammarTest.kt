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
package org.sonar.sslr.internal.grammar

import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.vm.CompilableGrammarRule

class MutableGrammarTest {
    @Test
    fun test() {
        val ruleKey = mock<GrammarRuleKey>()
        val rule = mock<CompilableGrammarRule>()
        val rootRuleKey = mock<GrammarRuleKey>()
        val rootRule = mock<CompilableGrammarRule>()
        val grammar = MutableGrammar(mapOf(Pair(ruleKey, rule), Pair(rootRuleKey, rootRule)), rootRuleKey)
        assertThat(grammar.rule(ruleKey)).isSameAs(rule)
        assertThat(grammar.getRootRule()).isSameAs(rootRule)
    }
}