//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarRuleKey

public class RuleRefExpression(private val ruleKey: GrammarRuleKey?) : NativeExpression() {
    public fun getRuleKey(): GrammarRuleKey? {
        return ruleKey
    }

    override fun execute(machine: Machine) {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return "Ref $ruleKey"
    }
}
