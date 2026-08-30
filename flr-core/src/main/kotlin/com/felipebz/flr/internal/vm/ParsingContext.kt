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

import com.felipebz.flr.grammar.ContextKey

/**
 * Immutable persistent context represented as a chain of bindings.
 *
 * A binding with [Binding.present] set to false deliberately masks an outer
 * binding; this is what makes an explicit withoutContext scope isolate its
 * contents. Lookup and equality traverse the binding chain, so parser
 * contexts are intended to remain shallow.
 */
internal class ParsingContext private constructor(
    private val parent: ParsingContext?,
    private val binding: Binding?
) {
    internal fun with(key: ContextKey<*>, value: Any?): ParsingContext {
        return ParsingContext(this, Binding(key, value, true))
    }

    internal fun without(key: ContextKey<*>): ParsingContext {
        return ParsingContext(this, Binding(key, null, false))
    }

    internal fun parent(): ParsingContext {
        return checkNotNull(parent)
    }

    internal fun contains(key: ContextKey<*>): Boolean {
        return find(key)?.present == true
    }

    internal fun matches(key: ContextKey<*>, expected: Any?): Boolean {
        val found = find(key)
        return found?.present == true && found.value == expected
    }

    private fun find(key: ContextKey<*>): Binding? {
        var current: ParsingContext? = this
        while (current != null) {
            val currentBinding = current.binding
            if (currentBinding != null && currentBinding.key === key) {
                return currentBinding
            }
            current = current.parent
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParsingContext && parent == other.parent && binding == other.binding
    }

    override fun hashCode(): Int {
        return 31 * (parent?.hashCode() ?: 0) + (binding?.hashCode() ?: 0)
    }

    private data class Binding(
        val key: ContextKey<*>,
        val value: Any?,
        val present: Boolean
    )

    internal companion object {
        val EMPTY: ParsingContext = ParsingContext(null, null)
    }
}
