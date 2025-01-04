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

import com.felipebz.flr.api.typed.GrammarBuilder
import com.felipebz.flr.examples.grammars.typed.api.*
import com.felipebz.flr.examples.grammars.typed.impl.InternalSyntaxToken
import com.felipebz.flr.examples.grammars.typed.impl.SyntaxList

public open class JsonGrammar(private val b: GrammarBuilder<InternalSyntaxToken?>, private val f: TreeFactory) {
    public open fun JSON(): JsonTree? {
        return b.nonterminal<JsonTree?>(JsonLexer.JSON).`is`(
            f.json(
                b.firstOf(
                    ARRAY(),
                    OBJECT()
                ),
                b.token(JsonLexer.EOF)
            )
        )
    }

    public open fun OBJECT(): ObjectTree {
        return b.nonterminal<ObjectTree>(JsonLexer.OBJECT).`is`(
            f.`object`(
                b.token(JsonLexer.LCURLYBRACE),
                b.optional(PAIR_LIST()),
                b.token(JsonLexer.RCURLYBRACE)
            )
        )
    }

    public open fun PAIR_LIST(): SyntaxList<PairTree?>? {
        return b.nonterminal<SyntaxList<PairTree?>?>().`is`(
            b.firstOf(
                f.pairList(PAIR(), b.token(JsonLexer.COMMA), PAIR_LIST()),
                f.pairList(PAIR())
            )
        )
    }

    public open fun PAIR(): PairTree? {
        return b.nonterminal<PairTree?>().`is`(
            f.pair(STRING(), b.token(JsonLexer.COLON), VALUE())
        )
    }

    public open fun ARRAY(): ArrayTree? {
        return b.nonterminal<ArrayTree?>(JsonLexer.ARRAY).`is`(
            f.array(
                b.token(JsonLexer.LBRACKET),
                b.optional(VALUE_LIST()),
                b.token(JsonLexer.RBRACKET)
            )
        )
    }

    public open fun VALUE_LIST(): SyntaxList<ValueTree?>? {
        return b.nonterminal<SyntaxList<ValueTree?>?>().`is`(
            b.firstOf(
                f.valueList(VALUE(), b.token(JsonLexer.COMMA), VALUE_LIST()),
                f.valueList(VALUE())
            )
        )
    }

    public open fun VALUE(): ValueTree? {
        return b.nonterminal<ValueTree?>(JsonLexer.VALUE).`is`(
            b.firstOf(
                STRING(),
                NUMBER(),
                OBJECT(),
                ARRAY(),
                BUILT_IN_VALUE()
            )
        )
    }

    public open fun STRING(): LiteralTree? {
        return b.nonterminal<LiteralTree?>().`is`(
            f.string(b.token(JsonLexer.STRING))
        )
    }

    public open fun NUMBER(): LiteralTree? {
        return b.nonterminal<LiteralTree?>().`is`(
            f.number(b.token(JsonLexer.NUMBER))
        )
    }

    public open fun BUILT_IN_VALUE(): BuiltInValueTree? {
        return b.nonterminal<BuiltInValueTree?>().`is`(
            f.buildInValue(
                b.firstOf(
                    b.token(JsonLexer.TRUE),
                    b.token(JsonLexer.FALSE),
                    b.token(JsonLexer.NULL)
                )
            )
        )
    }
}
