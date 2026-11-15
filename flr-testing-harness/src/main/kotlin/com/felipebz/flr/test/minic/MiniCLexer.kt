//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
            .withChannel(IdentifierAndKeywordChannel("[a-zA-Z]([a-zA-Z0-9_]*[a-zA-Z0-9])?+", true,
                Keywords.entries.toTypedArray()
            ))
            .withChannel(regexp(Literals.INTEGER, "[0-9]+"))
            .withChannel(commentRegexp("(?s)/\\*.*?\\*/"))
            .withChannel(PunctuatorChannel(*Punctuators.entries.toTypedArray()))
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
