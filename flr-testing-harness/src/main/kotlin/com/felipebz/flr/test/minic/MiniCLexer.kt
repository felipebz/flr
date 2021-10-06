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
package com.felipebz.flr.test.minic

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.IdentifierAndKeywordChannel
import com.felipebz.flr.impl.channel.PunctuatorChannel
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.commentRegexp
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.regexp

public object MiniCLexer {

    public fun create(): Lexer {
        return Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(IdentifierAndKeywordChannel("[a-zA-Z]([a-zA-Z0-9_]*[a-zA-Z0-9])?+", true, Keywords.values()))
            .withChannel(regexp(Literals.INTEGER, "[0-9]+"))
            .withChannel(commentRegexp("(?s)/\\*.*?\\*/"))
            .withChannel(PunctuatorChannel(*Punctuators.values()))
            .withChannel(BlackHoleChannel("[ \t\r\n]+"))
            .build()
    }

    public enum class Literals : TokenType {
        INTEGER;

        override val value: String = name

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }

    public enum class Punctuators(override val value: String) : TokenType {
        PAREN_L("("),
        PAREN_R(")"),
        BRACE_L("{"),
        BRACE_R("}"),
        EQ("="),
        COMMA(","),
        SEMICOLON(";"),
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        EQEQ("=="),
        NE("!="),
        LT("<"),
        LTE("<="),
        GT(">"),
        GTE(">="),
        INC("++"),
        DEC("--"),
        HASH("#");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }

    public enum class Keywords(override val value: String) : TokenType {
        STRUCT("struct"),
        INT("int"),
        VOID("void"),
        RETURN("return"),
        IF("if"),
        ELSE("else"),
        WHILE("while"),
        CONTINUE("continue"),
        BREAK("break");

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }
    }
}