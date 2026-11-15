//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.internal.matchers.Matcher

public class CompiledGrammar(
    public val program: InstructionProgram,
    private val rules: Map<GrammarRuleKey, CompilableGrammarRule>,
    public val rootRuleKey: GrammarRuleKey,
    public val rootRuleOffset: Int,
    internal val usesParserContext: Boolean
) {
    /** Resolved once per compiled grammar; `null` selects the ordinary machines. */
    internal val memoRetention: MemoRetention? = MemoRetention.of(program)

    public constructor(
        program: InstructionProgram,
        rules: Map<GrammarRuleKey, CompilableGrammarRule>,
        rootRuleKey: GrammarRuleKey,
        rootRuleOffset: Int
    ) : this(program, rules, rootRuleKey, rootRuleOffset, false)

    public val instructions: Array<Instruction>
        get() = program.instructions

    public val callTargetCount: Int
        get() = program.callTargetCount

    public fun getMatcher(ruleKey: GrammarRuleKey): Matcher? {
        return rules[ruleKey]
    }
}
