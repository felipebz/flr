//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api

/**
 * Each AST Node has a type which can be for instance the Rule of a Grammar, a language keyword or a language special token like a WORD.
 * When implementing the AstVisitor interface, its necessary to subscribe to a set of AstNodeType to be notified each time an AstNode with
 * the desired type is visited.
 *
 *
 * @see AstVisitor
 *
 * @see Grammar
 *
 * @see AstNode
 */
public interface AstNodeType
