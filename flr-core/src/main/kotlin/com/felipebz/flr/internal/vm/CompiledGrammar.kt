/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2026 Felipe Zorzo
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
