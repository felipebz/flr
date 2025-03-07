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
