//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.grammar

/**
 * Identifies a value in the parser context.
 *
 * Keys use object identity, not structural equality. Reuse the same key
 * instance for every expression that participates in one logical context.
 * Two different instances are independent, even when they have the same
 * generic type.
 *
 * Context values must be immutable for the duration of parsing and must have
 * stable equality semantics. Value equality participates in memoization
 * identity when parser memoization is enabled.
 */
public class ContextKey<T> {
    public constructor()
}
