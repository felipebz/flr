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
package com.felipebz.flr.examples.grammars

import com.felipebz.flr.api.Grammar
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder

/**
 * JSON grammar. See [http://json.org/](http://json.org/).
 */
public enum class JsonGrammar : GrammarRuleKey {
    JSON, ARRAY, OBJECT, PAIR, VALUE, STRING, NUMBER, TRUE, FALSE, NULL, WHITESPACE;

    public companion object {
        @JvmStatic
        public fun create(): Grammar {
            val b = LexerlessGrammarBuilder.create()
            b.rule(JSON).`is`(b.firstOf(ARRAY, OBJECT))
            b.rule(OBJECT).`is`("{", WHITESPACE, b.optional(PAIR, b.zeroOrMore(",", WHITESPACE, PAIR)), "}", WHITESPACE)
            b.rule(PAIR).`is`(STRING, ":", WHITESPACE, VALUE)
            b.rule(ARRAY)
                .`is`("[", WHITESPACE, b.optional(VALUE, b.zeroOrMore(",", WHITESPACE, VALUE)), "]", WHITESPACE)
            b.rule(STRING).`is`('"', b.regexp("([^\"\\\\]|\\\\([\"\\\\/bfnrt]|u[0-9a-fA-F]{4}))*+"), '"', WHITESPACE)
            b.rule(VALUE).`is`(b.firstOf(STRING, NUMBER, OBJECT, ARRAY, TRUE, FALSE, NULL), WHITESPACE)
            b.rule(NUMBER).`is`(b.regexp("-?+(0|[1-9][0-9]*+)(\\.[0-9]++)?+([eE][+-]?+[0-9]++)?+"))
            b.rule(TRUE).`is`("true")
            b.rule(FALSE).`is`("false")
            b.rule(NULL).`is`("null")
            b.rule(WHITESPACE).`is`(b.regexp("[ \n\r\t\u000C]*+"))
            return b.build()
        }
    }
}
