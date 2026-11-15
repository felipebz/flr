//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.test.minic.rules

import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExpressionTest : RuleTest() {
    @BeforeEach
    override fun init() {
        p.setRootRule(g.rule(MiniCGrammar.EXPRESSION))
    }

    @Test
    fun reallife() {
        assertThat(p)
            .matches("1")
            .matches("1 + 1")
            .matches("1 + 1 * 1")
            .matches("(1)")
            .matches("myVariable")
            .matches("myVariable = 0")
            .notMatches("myVariable = myVariable2 = 0")
            .matches("myFunction()")
            .matches("myFunction(arg1, arg2, 1*3)")
            .matches("myVariable++")
            .matches("++myVariable")
            .notMatches("++++myVariable")
            .matches("myVariable = i++")
            .matches("myVariable = myFunction(1, 3)*2")
            .matches("++((myVariable))")
    }
}
