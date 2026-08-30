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
