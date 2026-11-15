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

class StructTest : RuleTest() {
    @BeforeEach
    override fun init() {
        p.setRootRule(g.rule(MiniCGrammar.STRUCT_DEFINITION))
    }

    @Test
    fun reallife() {
        assertThat(p)
            .matches("struct my { int a; }")
            .matches("struct my { int a; int b; }")
    }
}
