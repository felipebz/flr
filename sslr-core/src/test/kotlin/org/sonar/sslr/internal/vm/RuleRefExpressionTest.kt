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
package org.sonar.sslr.internal.vm

import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.vm.Machine

class RuleRefExpressionTest {
    private val ruleKey = Mockito.mock(GrammarRuleKey::class.java)
    private val expression = RuleRefExpression(ruleKey)
    private val machine = Mockito.mock(Machine::class.java)
    @Test
    fun should_compile() {
        Assertions.assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        Assertions.assertThat(expression.getRuleKey()).isSameAs(ruleKey)
        Assertions.assertThat(expression.toString()).isEqualTo("Ref $ruleKey")
    }

    @Test
    fun can_not_be_executed() {
        assertThrows(UnsupportedOperationException::class.java) {
            expression.execute(machine)
        }
    }
}