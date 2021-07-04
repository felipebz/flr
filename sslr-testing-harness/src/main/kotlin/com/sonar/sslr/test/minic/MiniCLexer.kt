/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package com.sonar.sslr.test.minic

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.Lexer
import com.sonar.sslr.impl.channel.BlackHoleChannel
import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel
import com.sonar.sslr.impl.channel.PunctuatorChannel
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp

object MiniCLexer {
    @JvmStatic
    fun create(): Lexer {
        return Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(IdentifierAndKeywordChannel("[a-zA-Z]([a-zA-Z0-9_]*[a-zA-Z0-9])?+", true, Keywords.values()))
            .withChannel(regexp(Literals.INTEGER, "[0-9]+"))
            .withChannel(commentRegexp("(?s)/\\*.*?\\*/"))
            .withChannel(PunctuatorChannel(*Punctuators.values()))
            .withChannel(BlackHoleChannel("[ \t\r\n]+"))
            .build()
    }

    enum class Literals : TokenType {
        INTEGER;

        override val value: String
            get() {
                return name
            }

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }

    enum class Punctuators(override val value: String) : TokenType {
        PAREN_L("("), PAREN_R(")"), BRACE_L("{"), BRACE_R("}"), EQ("="), COMMA(","), SEMICOLON(";"), ADD("+"), SUB("-"), MUL(
            "*"
        ),
        DIV("/"), EQEQ("=="), NE("!="), LT("<"), LTE("<="), GT(">"), GTE(">="), INC("++"), DEC("--"), HASH("#");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }

    enum class Keywords(override val value: String) : TokenType {
        STRUCT("struct"), INT("int"), VOID("void"), RETURN("return"), IF("if"), ELSE("else"), WHILE("while"), CONTINUE("continue"), BREAK(
            "break"
        );

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }

        companion object {
            fun keywordValues(): Array<String> {
                return values().map { it.value }.toTypedArray()
            }
        }
    }
}