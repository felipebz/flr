/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.sslr.examples.grammars.typed

import com.sonar.sslr.api.typed.Optional
import org.sonar.sslr.examples.grammars.typed.api.*
import org.sonar.sslr.examples.grammars.typed.impl.*

open class TreeFactory {
    open fun json(arrayOrObject: Tree?, eof: InternalSyntaxToken?): JsonTree {
        return JsonTreeImpl(arrayOrObject!!)
    }

    open fun buildInValue(token: InternalSyntaxToken?): BuiltInValueTree {
        return BuiltInValueTreeImpl(token!!)
    }

    open fun number(token: InternalSyntaxToken?): LiteralTree {
        return LiteralTreeImpl(token!!)
    }

    open fun string(token: InternalSyntaxToken?): LiteralTree {
        return LiteralTreeImpl(token!!)
    }

    open fun valueList(value: ValueTree?): SyntaxList<ValueTree?> {
        return SyntaxList(value, null, null)
    }

    open fun valueList(
        value: ValueTree?,
        commaToken: InternalSyntaxToken?,
        next: SyntaxList<ValueTree?>?
    ): SyntaxList<ValueTree?> {
        return SyntaxList(value, commaToken, next)
    }

    open fun array(
        openBracketToken: InternalSyntaxToken?,
        values: Optional<SyntaxList<ValueTree?>?>,
        closeBracketToken: InternalSyntaxToken?
    ): ArrayTree {
        return ArrayTreeImpl(openBracketToken, values.orNull(), closeBracketToken)
    }

    open fun pair(string: LiteralTree?, colonToken: InternalSyntaxToken?, value: ValueTree?): PairTree {
        return PairTreeImpl(string!!, colonToken!!, value!!)
    }

    open fun pairList(pair: PairTree?): SyntaxList<PairTree?> {
        return SyntaxList(pair, null, null)
    }

    open fun pairList(pair: PairTree?, commaToken: InternalSyntaxToken?, next: SyntaxList<PairTree?>?): SyntaxList<PairTree?> {
        return SyntaxList(pair, commaToken, next)
    }

    open fun `object`(
        openCurlyBraceToken: InternalSyntaxToken?,
        pairs: Optional<SyntaxList<PairTree?>?>,
        closeCurlyBraceToken: InternalSyntaxToken?
    ): ObjectTree {
        return ObjectTreeImpl(openCurlyBraceToken!!, pairs.orNull(), closeCurlyBraceToken!!)
    }
}