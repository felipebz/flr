/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.api

/**
 *
 * This interface is not intended to be implemented by clients.
 */
public interface Rule : AstNodeType {
    /**
     * Allows to provide definition of a grammar rule.
     *
     *
     * **Note:** this method can be called only once for a rule. If it is called more than once, an IllegalStateException will be thrown.
     *
     * @param e expression of grammar that defines this rule
     * @return this (for method chaining)
     * @throws IllegalStateException if definition has been already done
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#is(Object)} instead.")
    public fun `is`(vararg e: Any): Rule

    /**
     * Allows to override definition of a grammar rule.
     *
     *
     * This method has the same effect as [.is], except that it can be called more than once to redefine a rule from scratch.
     *
     * @param e expression of grammar that defines this rule
     * @return this (for method chaining)
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#override(Object)} instead.")
    public fun override(vararg e: Any): Rule

    /**
     * Indicates that grammar rule should not lead to creation of AST node - its children should be attached directly to its parent.
     *
     */
    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#skip()} instead.")
    public fun skip()

    /**
     * Indicates that grammar rule should not lead to creation of AST node if it has exactly one child.
     *
     */
    @Deprecated("in 1.19, use {@link com.felipebz.flr.grammar.GrammarRuleBuilder#skipIfOneChild()} instead.")
    public fun skipIfOneChild()
}
