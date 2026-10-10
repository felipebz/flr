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

    /**
     * Memoizes successful matches of this rule, like [LexerfulGrammarBuilder.buildWithMemoizationOfMatchesForAllRules]
     * does for every rule, and also keeps them reusable after another rule's match replaces them.
     *
     * The memo holds one result per input position: whichever memoizing rule matched there last. A later call of this
     * rule at that position finds another rule's result and runs again, which can make nested constructs parse in
     * exponential time. A retained result is reused under the same conditions as a memo entry (same rule, equal parser
     * context); a result produced while parse errors are ignored (inside `nextNot` or a lexerless `token`) is only
     * reused while errors are ignored, because running the rule with error reporting can record a further error position.
     *
     * **Eligibility.** Reusing a result means the rule body does not run again. That is only equivalent to running
     * it when the outcome depends on nothing but the input position, the rule and the parser context: no mutable state
     * outside the parse, and no custom `NativeExpression` with observable side effects
     * or state that persists between runs. Retention makes none of that safe; it only extends the lifetime of results
     * that the memo would already have reused had they not been replaced.
     *
     * **Memory.** Per parse, each retaining rule needs up to one reference per input position, allocated on the first
     * time a result of that rule is replaced. Once a parser context has been activated, an equally sized array of
     * contexts is added, plus one bit per input position for results created while errors were ignored. Retained
     * results stay reachable until the parse ends. Only one result per rule and position is kept: replacing a
     * retained result of the same rule (created in another parser context) drops the older one. Retaining rules do not
     * displace each other. Grammars without retaining rules use the unchanged machines and allocate nothing extra.
     *
     * Retention only pays off for rules whose replaced results are requested again; `ParsingProfiler` shows these as
     * memo matcher misses. Configure it before the grammar is compiled; the choice is fixed per compiled grammar.
     *
     * @throws GrammarException if the rule implementation does not support memo retention
     * @since 1.7
     */
    public fun enableMemoRetention()
}
