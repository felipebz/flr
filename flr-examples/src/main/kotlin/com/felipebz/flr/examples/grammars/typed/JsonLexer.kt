//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.grammar.LexerlessGrammarBuilder.Companion.create

public enum class JsonLexer : GrammarRuleKey {
    JSON, OBJECT, ARRAY, VALUE, TRUE, FALSE, NULL, STRING, NUMBER, LCURLYBRACE, RCURLYBRACE, LBRACKET, RBRACKET, COMMA, COLON, EOF, WHITESPACE;

    public companion object {
        @JvmStatic
        public fun createGrammarBuilder(): LexerlessGrammarBuilder {
            val b = create()
            punctuator(b, LCURLYBRACE, "{")
            punctuator(b, RCURLYBRACE, "}")
            punctuator(b, LBRACKET, "[")
            punctuator(b, RBRACKET, "]")
            punctuator(b, COMMA, ",")
            punctuator(b, COLON, ":")
            b.rule(TRUE).`is`("true", WHITESPACE)
            b.rule(FALSE).`is`("false", WHITESPACE)
            b.rule(NULL).`is`("null", WHITESPACE)
            b.rule(WHITESPACE).`is`(b.regexp("[ \n\r\t\u000C]*+"))
            b.rule(NUMBER).`is`(b.regexp("-?+(0|[1-9][0-9]*+)(\\.[0-9]++)?+([eE][+-]?+[0-9]++)?+"), WHITESPACE)
            b.rule(STRING).`is`(b.regexp("\"([^\"\\\\]|\\\\([\"\\\\/bfnrt]|u[0-9a-fA-F]{4}))*+\""), WHITESPACE)
            b.rule(EOF).`is`(b.token(GenericTokenType.EOF, b.endOfInput()))
            b.setRootRule(JSON)
            return b
        }

        private fun punctuator(b: LexerlessGrammarBuilder, ruleKey: GrammarRuleKey, value: String) {
            b.rule(ruleKey).`is`(value, WHITESPACE)
        }
    }
}
