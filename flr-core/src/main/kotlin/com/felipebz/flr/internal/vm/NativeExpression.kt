//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

public abstract class NativeExpression : Instruction(), ParsingExpression {
    override fun compile(compiler: CompilationHandler): Array<Instruction> {
        return arrayOf(this)
    }
}
