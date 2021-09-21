/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package org.sonar.sslr.internal.grammar

import com.sonar.sslr.api.Rule
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.internal.vm.CompilableGrammarRule
import org.sonar.sslr.parser.LexerlessGrammar

public class MutableGrammar(
    private val rules: Map<GrammarRuleKey, CompilableGrammarRule>,
    private val rootRuleKey: GrammarRuleKey?
) : LexerlessGrammar() {
    override fun rule(ruleKey: GrammarRuleKey): Rule {
        return requireNotNull(rules[ruleKey])
    }

    override val rootRule: Rule
        get() = rule(checkNotNull(rootRuleKey))
}