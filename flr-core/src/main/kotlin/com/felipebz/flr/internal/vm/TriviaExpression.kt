//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.Trivia.TriviaKind
import com.felipebz.flr.internal.matchers.Matcher

public class TriviaExpression(private val triviaKind: TriviaKind, private val subExpression: ParsingExpression) : Matcher,
    ParsingExpression {
    public fun getTriviaKind(): TriviaKind {
        return triviaKind
    }

    /**
     * Compiles this expression into a sequence of instructions:
     * <pre>
     * Call L1
     * Jump L2
     * L1: subExpression
     * Return
     * L2: ...
    </pre> *
     */
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        return TokenExpression.compile(compiler, this, subExpression)
    }

    override fun toString(): String {
        return "Trivia $triviaKind[$subExpression]"
    }
}
