//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarRuleKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock

class RuleRefExpressionTest {
    private val ruleKey = mock<GrammarRuleKey>()
    private val expression = RuleRefExpression(ruleKey)
    private val machine = mock<Machine>()

    @Test
    fun should_compile() {
        assertThat(expression.compile(CompilationHandler())).containsOnly(expression)
        assertThat(expression.getRuleKey()).isSameAs(ruleKey)
        assertThat(expression.toString()).isEqualTo("Ref $ruleKey")
    }

    @Test
    fun can_not_be_executed() {
        assertThrows<UnsupportedOperationException> {
            expression.execute(machine)
        }
    }
}
