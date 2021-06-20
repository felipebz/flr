/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package com.sonar.sslr.api

import java.util.*

/**
 * This class encapsulates the actions to be performed by a preprocessor.
 */
@Deprecated("in 1.20, use your own preprocessor API instead.")
class PreprocessorAction(numberOfConsumedTokens: Int, private val triviaToInject: List<Trivia>, private val tokensToInject: List<Token>) {
    private val numberOfConsumedTokens: Int
    fun getNumberOfConsumedTokens(): Int {
        return numberOfConsumedTokens
    }

    fun getTriviaToInject(): List<Trivia> {
        return triviaToInject
    }

    fun getTokensToInject(): List<Token> {
        return tokensToInject
    }

    companion object {
        /**
         *
         *
         * Use this no operation preprocessor action for improved readability and performances.
         *
         *
         *
         *
         * Equivalent to: <tt>new PreprocessorAction(0, new ArrayList&lt;Trivia&gt;(), new ArrayList&lt;Token&gt;());</tt>
         *
         */
        val NO_OPERATION = PreprocessorAction(0, ArrayList(), ArrayList())
    }

    /**
     *
     *
     * Construct a preprocessor action.
     *
     *
     *
     *
     * The actions are executed in this order:
     *
     *  1. Deletions of tokens, handled by numberOfConsumedTokens
     *  1. Injections of trivia, handled by triviaToInject
     *  1. Injections of tokens, handled by tokensToInject
     *
     *
     *
     *
     *
     * Preprocessor actions are executed as follows:
     *
     *  1. If numberOfConsumedTokens is greater than 0, then this number of tokens are deleted. Their trivia is added to a pending list of
     * trivia. The preprocessor will not be called on deleted tokens.
     *  1. All trivia from triviaToInject are added to the same pending list of trivia
     *  1. All tokens from tokensToInject are injected. If present, the first token of tokensToInject is augmented with the pending trivia,
     * which is then cleared. If not present, the pending trivia is left unchanged.
     *  1. Finally, if numberOfConsumedTokens was 0, the current token is injected, with any pending trivia which is then cleared.
     *
     *
     *
     * A few examples:
     *
     *  * No operation action: <tt>new PreprocessorAction(0, new ArrayList&lt;Trivia&gt;(), new ArrayList&lt;Token&gt;());</tt>
     *  * Delete current token action: <tt>new PreprocessorAction(1, new ArrayList&lt;Trivia&gt;(), new ArrayList&lt;Token&gt;());</tt>
     *  * Modify current token action: <tt>new PreprocessorAction(1, new ArrayList&lt;Trivia&gt;(), Arrays.asList(modifiedToken));</tt>
     *  * Inject trivia to current token action: <tt>new PreprocessorAction(0, Arrays.asList(newTrivia), new ArrayList&lt;Token&gt;());</tt>
     *
     *
     * @param numberOfConsumedTokens
     * Number of tokens consumed by the preprocessor, which can be 0. Consumed tokens are deleted and will not lead to successive
     * calls to the preprocessor.
     * @param triviaToInject
     * Trivia to inject.
     * @param tokensToInject
     * Tokens to inject. Injected tokens will not lead to successive calls to the preprocessor.
     */
    init {
        require(numberOfConsumedTokens >= 0) { "numberOfConsumedTokens($numberOfConsumedTokens) must be greater or equal to 0" }
        Objects.requireNonNull(triviaToInject, "triviaToInject cannot be null")
        Objects.requireNonNull(tokensToInject, "tokensToInject cannot be null")
        this.numberOfConsumedTokens = numberOfConsumedTokens
    }
}