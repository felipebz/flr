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
