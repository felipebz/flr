//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
