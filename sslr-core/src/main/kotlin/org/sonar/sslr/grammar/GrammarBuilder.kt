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
package org.sonar.sslr.grammar

import org.sonar.sslr.internal.vm.*

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
abstract class GrammarBuilder {
    /**
     * Allows to describe rule.
     * Result of this method should be used only for execution of methods in it, i.e. you should not save reference on it.
     * No guarantee that this method always returns the same instance for the same key of rule.
     */
    abstract fun rule(ruleKey: GrammarRuleKey): GrammarRuleBuilder

    /**
     * Allows to specify that given rule should be root for grammar.
     */
    abstract fun setRootRule(ruleKey: GrammarRuleKey)

    /**
     * Creates parsing expression - "sequence".
     * During execution of this expression parser will sequentially execute all sub-expressions.
     * This expression succeeds only if all sub-expressions succeed.
     *
     * @param e1  first sub-expression
     * @param e2  second sub-expression
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    fun sequence(e1: Any, e2: Any): Any {
        return SequenceExpression(convertToExpression(e1), convertToExpression(e2))
    }

    /**
     * Creates parsing expression - "sequence".
     * See [.sequence] for more details.
     *
     * @param e1  first sub-expression
     * @param e2  second sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    fun sequence(e1: Any, e2: Any, vararg rest: Any): Any {
        return SequenceExpression(*convertToExpressions(e1, e2, rest))
    }

    /**
     * Creates parsing expression - "first of".
     * During the execution of this expression parser execute sub-expressions in order until one succeeds.
     * This expressions succeeds if any sub-expression succeeds.
     *
     *
     * Be aware that in expression `firstOf("foo", sequence("foo", "bar"))` second sub-expression will never be executed.
     *
     * @param e1  first sub-expression
     * @param e2  second sub-expression
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    fun firstOf(e1: Any, e2: Any): Any {
        return FirstOfExpression(convertToExpression(e1), convertToExpression(e2))
    }

    /**
     * Creates parsing expression - "first of".
     * See [.firstOf] for more details.
     *
     * @param e1  first sub-expression
     * @param e2  second sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     */
    fun firstOf(e1: Any, e2: Any, vararg rest: Any): Any {
        return FirstOfExpression(*convertToExpressions(e1, e2, rest))
    }

    /**
     * Creates parsing expression - "optional".
     * During execution of this expression parser will execute sub-expression once.
     * This expression always succeeds, with an empty match if sub-expression fails.
     *
     *
     * Be aware that this expression is greedy, i.e. expression `sequence(optional("foo"), "foo")` will never succeed.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    fun optional(e: Any): Any {
        return OptionalExpression(convertToExpression(e))
    }

    /**
     * Creates parsing expression - "optional".
     * Convenience method equivalent to calling `optional(sequence(e, rest))`.
     *
     * @param e1  first sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @see .optional
     * @see .sequence
     */
    fun optional(e1: Any, vararg rest: Any): Any {
        return OptionalExpression(SequenceExpression(*convertToExpressions(e1, rest)))
    }

    /**
     * Creates parsing expression - "one or more".
     * During execution of this expression parser will repeatedly try sub-expression until it fails.
     * This expression succeeds only if sub-expression succeeds at least once.
     *
     *
     * Be aware that:
     *
     *  * This expression is a greedy, i.e. expression `sequence(oneOrMore("foo"), "foo")` will never succeed.
     *  * Sub-expression must not allow empty matches, i.e. for expression `oneOrMore(optional("foo"))` parser will report infinite loop.
     *
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    fun oneOrMore(e: Any): Any {
        return OneOrMoreExpression(convertToExpression(e))
    }

    /**
     * Creates parsing expression - "one or more".
     * Convenience method equivalent to calling `oneOrMore(sequence(e1, rest))`.
     *
     * @param e1  first sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @see .oneOrMore
     * @see .sequence
     */
    fun oneOrMore(e1: Any, vararg rest: Any): Any {
        return OneOrMoreExpression(SequenceExpression(*convertToExpressions(e1, rest)))
    }

    /**
     * Creates parsing expression - "zero or more".
     * During execution of this expression parser will repeatedly try sub-expression until it fails.
     * This expression always succeeds, with an empty match if sub-expression fails.
     *
     *
     * Be aware that:
     *
     *  * This expression is greedy, i.e. expression `sequence(zeroOrMore("foo"), "foo")` will never succeed.
     *  * Sub-expression must not allow empty matches, i.e. for expression `zeroOrMore(optional("foo"))` parser will report infinite loop.
     *
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    fun zeroOrMore(e: Any): Any {
        return ZeroOrMoreExpression(convertToExpression(e))
    }

    /**
     * Creates parsing expression - "zero or more".
     * Convenience method equivalent to calling `zeroOrMore(sequence(e1, rest))`.
     *
     * @param e1  sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @see .zeroOrMore
     * @see .sequence
     */
    fun zeroOrMore(e1: Any, vararg rest: Any): Any {
        return ZeroOrMoreExpression(SequenceExpression(*convertToExpressions(e1, rest)))
    }

    /**
     * Creates parsing expression - "next".
     * During execution of this expression parser will execute sub-expression once.
     * This expression succeeds only if sub-expression succeeds, but never consumes any input.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    fun next(e: Any): Any {
        return NextExpression(convertToExpression(e))
    }

    /**
     * Creates parsing expression - "next".
     * Convenience method equivalent to calling `next(sequence(e1, rest))`.
     *
     * @param e1  first sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @see .next
     * @see .sequence
     */
    fun next(e1: Any, vararg rest: Any): Any {
        return NextExpression(SequenceExpression(*convertToExpressions(e1, rest)))
    }

    /**
     * Creates parsing expression - "next not".
     * During execution of this expression parser will execute sub-expression once.
     * This expression succeeds only if sub-expression fails.
     *
     * @param e  sub-expression
     * @throws IllegalArgumentException if given argument is not a parsing expression
     */
    fun nextNot(e: Any): Any {
        return NextNotExpression(convertToExpression(e))
    }

    /**
     * Creates parsing expression - "next not".
     * Convenience method equivalent to calling `nextNot(sequence(e1, rest))`.
     *
     * @param e1  sub-expression
     * @param rest  rest of sub-expressions
     * @throws IllegalArgumentException if any of given arguments is not a parsing expression
     * @see .nextNot
     * @see .sequence
     */
    fun nextNot(e1: Any, vararg rest: Any): Any {
        return NextNotExpression(SequenceExpression(*convertToExpressions(e1, rest)))
    }

    /**
     * Creates parsing expression - "nothing".
     * This expression always fails.
     */
    fun nothing(): Any {
        return NothingExpression.INSTANCE
    }

    abstract fun convertToExpression(e: Any): ParsingExpression

    fun convertToExpressions(e1: Any, rest: Array<out Any>): Array<out ParsingExpression> {
        val result = arrayOfNulls<ParsingExpression>(1 + rest.size)
        result[0] = convertToExpression(e1)
        for (i in rest.indices) {
            result[1 + i] = convertToExpression(rest[i])
        }
        return result.requireNoNulls()
    }

    private fun convertToExpressions(e1: Any, e2: Any, rest: Array<out Any>): Array<ParsingExpression> {
        val result = arrayOfNulls<ParsingExpression>(2 + rest.size)
        result[0] = convertToExpression(e1)
        result[1] = convertToExpression(e2)
        for (i in rest.indices) {
            result[2 + i] = convertToExpression(rest[i])
        }
        return result.requireNoNulls()
    }

    /**
     * Adapts [CompilableGrammarRule] to be used as [GrammarRuleBuilder].
     */
    internal class RuleBuilder(private val b: GrammarBuilder, private val delegate: CompilableGrammarRule) :
        GrammarRuleBuilder {
        override fun `is`(e: Any): GrammarRuleBuilder {
            if (delegate.expression != null) {
                throw GrammarException("The rule '" + delegate.ruleKey + "' has already been defined somewhere in the grammar.")
            }
            delegate.expression = b.convertToExpression(e)
            return this
        }

        override fun `is`(e: Any, vararg rest: Any): GrammarRuleBuilder {
            val expressions = b.convertToExpressions(e, rest)
            return `is`(SequenceExpression(*expressions))
        }

        override fun override(e: Any): GrammarRuleBuilder {
            delegate.expression = b.convertToExpression(e)
            return this
        }

        override fun override(e: Any, vararg rest: Any): GrammarRuleBuilder {
            return override(SequenceExpression(*b.convertToExpressions(e, rest)))
        }

        override fun skip() {
            delegate.skip()
        }

        override fun skipIfOneChild() {
            delegate.skipIfOneChild()
        }
    }
}