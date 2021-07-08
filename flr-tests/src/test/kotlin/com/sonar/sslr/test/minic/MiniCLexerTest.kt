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

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.test.lexer.LexerMatchers.hasComment
import com.sonar.sslr.test.lexer.LexerMatchers.hasToken
import com.sonar.sslr.test.minic.MiniCLexer.Literals
import com.sonar.sslr.test.minic.MiniCLexer.Punctuators
import org.junit.Assert
import org.junit.Test

class MiniCLexerTest {
    var lexer = MiniCLexer.create()
    @Test
    fun lexIdentifiers() {
        Assert.assertThat(lexer.lex("abc"), hasToken("abc", GenericTokenType.IDENTIFIER))
        Assert.assertThat(lexer.lex("abc0"), hasToken("abc0", GenericTokenType.IDENTIFIER))
        Assert.assertThat(lexer.lex("abc_0"), hasToken("abc_0", GenericTokenType.IDENTIFIER))
        Assert.assertThat(lexer.lex("i"), hasToken("i", GenericTokenType.IDENTIFIER))
    }

    @Test
    fun lexIntegers() {
        Assert.assertThat(lexer.lex("0"), hasToken("0", Literals.INTEGER))
        Assert.assertThat(lexer.lex("000"), hasToken("000", Literals.INTEGER))
        Assert.assertThat(lexer.lex("1234"), hasToken("1234", Literals.INTEGER))
    }

    @Test
    fun lexKeywords() {
        Assert.assertThat(lexer.lex("int"), hasToken(MiniCLexer.Keywords.INT))
        Assert.assertThat(lexer.lex("void"), hasToken(MiniCLexer.Keywords.VOID))
        Assert.assertThat(lexer.lex("return"), hasToken(MiniCLexer.Keywords.RETURN))
        Assert.assertThat(lexer.lex("if"), hasToken(MiniCLexer.Keywords.IF))
        Assert.assertThat(lexer.lex("else"), hasToken(MiniCLexer.Keywords.ELSE))
        Assert.assertThat(lexer.lex("while"), hasToken(MiniCLexer.Keywords.WHILE))
        Assert.assertThat(lexer.lex("break"), hasToken(MiniCLexer.Keywords.BREAK))
        Assert.assertThat(lexer.lex("continue"), hasToken(MiniCLexer.Keywords.CONTINUE))
        Assert.assertThat(lexer.lex("struct"), hasToken(MiniCLexer.Keywords.STRUCT))
    }

    @Test
    fun lexComments() {
        Assert.assertThat(lexer.lex("/*test*/"), hasComment("/*test*/"))
        Assert.assertThat(lexer.lex("/*test*/*/"), hasComment("/*test*/"))
        Assert.assertThat(lexer.lex("/*test/* /**/"), hasComment("/*test/* /**/"))
        Assert.assertThat(lexer.lex("/*test1\ntest2\ntest3*/"), hasComment("/*test1\ntest2\ntest3*/"))
    }

    @Test
    fun lexPunctuators() {
        Assert.assertThat(lexer.lex("("), hasToken(Punctuators.PAREN_L))
        Assert.assertThat(lexer.lex(")"), hasToken(Punctuators.PAREN_R))
        Assert.assertThat(lexer.lex("{"), hasToken(Punctuators.BRACE_L))
        Assert.assertThat(lexer.lex("}"), hasToken(Punctuators.BRACE_R))
        Assert.assertThat(lexer.lex("="), hasToken(Punctuators.EQ))
        Assert.assertThat(lexer.lex(","), hasToken(Punctuators.COMMA))
        Assert.assertThat(lexer.lex(";"), hasToken(Punctuators.SEMICOLON))
        Assert.assertThat(lexer.lex("+"), hasToken(Punctuators.ADD))
        Assert.assertThat(lexer.lex("-"), hasToken(Punctuators.SUB))
        Assert.assertThat(lexer.lex("*"), hasToken(Punctuators.MUL))
        Assert.assertThat(lexer.lex("/"), hasToken(Punctuators.DIV))
        Assert.assertThat(lexer.lex("<"), hasToken(Punctuators.LT))
        Assert.assertThat(lexer.lex("<="), hasToken(Punctuators.LTE))
        Assert.assertThat(lexer.lex(">"), hasToken(Punctuators.GT))
        Assert.assertThat(lexer.lex(">="), hasToken(Punctuators.GTE))
        Assert.assertThat(lexer.lex("=="), hasToken(Punctuators.EQEQ))
        Assert.assertThat(lexer.lex("!="), hasToken(Punctuators.NE))
        Assert.assertThat(lexer.lex("++"), hasToken(Punctuators.INC))
        Assert.assertThat(lexer.lex("--"), hasToken(Punctuators.DEC))
    }
}