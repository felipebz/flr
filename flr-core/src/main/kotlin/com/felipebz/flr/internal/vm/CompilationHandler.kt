//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public open class CompilationHandler {
    internal var usesParserContext: Boolean = false
        private set

    internal fun markParserContextUsed() {
        usesParserContext = true
    }

    public open fun compile(expression: ParsingExpression): Array<Instruction> {
        return expression.compile(this)
    }
}
