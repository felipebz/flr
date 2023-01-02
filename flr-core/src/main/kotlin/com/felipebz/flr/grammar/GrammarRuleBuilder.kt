/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
 * This interface contains methods used to describe rule of grammar.
 *
 *
 * This interface is not intended to be implemented by clients.
 *
 * @since 1.18
 * @see LexerlessGrammarBuilder.rule
 * @see LexerfulGrammarBuilder.rule
 */
public interface GrammarRuleBuilder {
    /**
     * Allows to provide definition of a grammar rule.
     *
     *
     * **Note:** this method can be called only once for a rule. If it is called more than once, an GrammarException will be thrown.
     *
     * @param e  expression of grammar
     * @return this (for method chaining)
     * @throws GrammarException if definition has been already done
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    public fun `is`(e: Any): GrammarRuleBuilder

    /**
     * Convenience method equivalent to calling `is(grammarBuilder.sequence(e, rest))`.
     *
     * @param e  expression of grammar
     * @param rest  rest of expressions
     * @return this (for method chaining)
     * @throws GrammarException if definition has been already done
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @see .is
     */
    public fun `is`(e: Any, vararg rest: Any): GrammarRuleBuilder

    /**
     * Allows to override definition of a grammar rule.
     *
     *
     * This method has the same effect as [.is], except that it can be called more than once to redefine a rule from scratch.
     *
     * @param e  expression of grammar
     * @throws IllegalArgumentException if given argument is not a parsing expression
     * @return this (for method chaining)
     */
    public fun override(e: Any): GrammarRuleBuilder

    /**
     * Convenience method equivalent to calling `override(grammarBuilder.sequence(e, rest))`.
     *
     * @param e  expression of grammar
     * @param rest  rest of expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @return this (for method chaining)
     * @see .override
     */
    public fun override(e: Any, vararg rest: Any): GrammarRuleBuilder

    /**
     * Indicates that grammar rule should not lead to creation of AST node - its children should be attached directly to its parent.
     */
    public fun skip()

    /**
     * Indicates that grammar rule should not lead to creation of AST node if it has exactly one child.
     */
    public fun skipIfOneChild()
}