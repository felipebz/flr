//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.test.minic.rules

import com.felipebz.flr.api.Rule
import com.felipebz.flr.test.minic.MiniCParser

abstract class RuleTest {
    protected val p = MiniCParser.create()
    protected val g = p.grammar
    val testedRule: Rule
        get() = p.rootRule

    abstract fun init()
}
