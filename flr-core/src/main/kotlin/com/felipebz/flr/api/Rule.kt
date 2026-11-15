//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

/**
 * A rule of a grammar, as the type of the AST nodes it creates. Rules are defined through
 * [com.felipebz.flr.grammar.LexerfulGrammarBuilder] and [com.felipebz.flr.grammar.LexerlessGrammarBuilder].
 *
 * This interface is not intended to be implemented by clients.
 */
public interface Rule : AstNodeType
