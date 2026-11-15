//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
