//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.impl.matcher.Matcher

/**
 * Parsing expression.
 */
public interface ParsingExpression : Matcher {
    /**
     * Compiles this expression into a sequence of instructions.
     */
    public fun compile(compiler: CompilationHandler): Array<Instruction>
}
