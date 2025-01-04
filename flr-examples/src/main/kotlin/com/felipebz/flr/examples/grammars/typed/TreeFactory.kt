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
package com.felipebz.flr.examples.grammars.typed

import com.felipebz.flr.examples.grammars.typed.api.*
import com.felipebz.flr.examples.grammars.typed.impl.*
import java.util.*

public open class TreeFactory {
    public open fun json(arrayOrObject: Tree?, eof: InternalSyntaxToken?): JsonTree {
        return JsonTreeImpl(arrayOrObject!!)
    }

    public open fun buildInValue(token: InternalSyntaxToken?): BuiltInValueTree {
        return BuiltInValueTreeImpl(token!!)
    }

    public open fun number(token: InternalSyntaxToken?): LiteralTree {
        return LiteralTreeImpl(token!!)
    }

    public open fun string(token: InternalSyntaxToken?): LiteralTree {
        return LiteralTreeImpl(token!!)
    }

    public open fun valueList(value: ValueTree?): SyntaxList<ValueTree?> {
        return SyntaxList(value, null, null)
    }

    public open fun valueList(
        value: ValueTree?,
        commaToken: InternalSyntaxToken?,
        next: SyntaxList<ValueTree?>?
    ): SyntaxList<ValueTree?> {
        return SyntaxList(value, commaToken, next)
    }

    public open fun array(
        openBracketToken: InternalSyntaxToken?,
        values: Optional<SyntaxList<ValueTree?>?>?,
        closeBracketToken: InternalSyntaxToken?
    ): ArrayTree {
        return ArrayTreeImpl(openBracketToken, values?.orElse(null), closeBracketToken)
    }

    public open fun pair(string: LiteralTree?, colonToken: InternalSyntaxToken?, value: ValueTree?): PairTree {
        return PairTreeImpl(string!!, colonToken!!, value!!)
    }

    public open fun pairList(pair: PairTree?): SyntaxList<PairTree?> {
        return SyntaxList(pair, null, null)
    }

    public open fun pairList(pair: PairTree?, commaToken: InternalSyntaxToken?, next: SyntaxList<PairTree?>?): SyntaxList<PairTree?> {
        return SyntaxList(pair, commaToken, next)
    }

    public open fun `object`(
        openCurlyBraceToken: InternalSyntaxToken?,
        pairs: Optional<SyntaxList<PairTree?>?>?,
        closeCurlyBraceToken: InternalSyntaxToken?
    ): ObjectTree {
        return ObjectTreeImpl(openCurlyBraceToken!!, pairs?.orElse(null), closeCurlyBraceToken!!)
    }
}
