//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.grammar

import com.felipebz.flr.api.AstNodeType

/**
 * A marker interface for key of grammar rule.
 *
 *
 * For example:
 * <pre>
 * public enum YourGrammarRules implements GrammarRuleKey {
 * YOUR_RULE_KEY;
 * }
</pre> *
 */
public interface GrammarRuleKey : AstNodeType
