/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.test.minic.MiniCLexer.Literals
import com.felipebz.flr.test.minic.MiniCLexer.Punctuators
import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class MiniCLexerTest {
    private var lexer = MiniCLexer.create()
    @Test
    fun lexIdentifiers() {
        assertThat(lexer.lex("abc")).hasToken("abc", GenericTokenType.IDENTIFIER)
        assertThat(lexer.lex("abc0")).hasToken("abc0", GenericTokenType.IDENTIFIER)
        assertThat(lexer.lex("abc_0")).hasToken("abc_0", GenericTokenType.IDENTIFIER)
        assertThat(lexer.lex("i")).hasToken("i", GenericTokenType.IDENTIFIER)
    }

    @Test
    fun lexIntegers() {
        assertThat(lexer.lex("0")).hasToken("0", Literals.INTEGER)
        assertThat(lexer.lex("000")).hasToken("000", Literals.INTEGER)
        assertThat(lexer.lex("1234")).hasToken("1234", Literals.INTEGER)
    }

    @Test
    fun lexKeywords() {
        assertThat(lexer.lex("int")).hasToken(MiniCLexer.Keywords.INT)
        assertThat(lexer.lex("void")).hasToken(MiniCLexer.Keywords.VOID)
        assertThat(lexer.lex("return")).hasToken(MiniCLexer.Keywords.RETURN)
        assertThat(lexer.lex("if")).hasToken(MiniCLexer.Keywords.IF)
        assertThat(lexer.lex("else")).hasToken(MiniCLexer.Keywords.ELSE)
        assertThat(lexer.lex("while")).hasToken(MiniCLexer.Keywords.WHILE)
        assertThat(lexer.lex("break")).hasToken(MiniCLexer.Keywords.BREAK)
        assertThat(lexer.lex("continue")).hasToken(MiniCLexer.Keywords.CONTINUE)
        assertThat(lexer.lex("struct")).hasToken(MiniCLexer.Keywords.STRUCT)
    }

    @Test
    fun lexComments() {
        assertThat(lexer.lex("/*test*/")).hasComment("/*test*/")
        assertThat(lexer.lex("/*test*/*/")).hasComment("/*test*/")
        assertThat(lexer.lex("/*test/* /**/")).hasComment("/*test/* /**/")
        assertThat(lexer.lex("/*test1\ntest2\ntest3*/")).hasComment("/*test1\ntest2\ntest3*/")
    }

    @Test
    fun lexPunctuators() {
        assertThat(lexer.lex("(")).hasToken(Punctuators.PAREN_L)
        assertThat(lexer.lex(")")).hasToken(Punctuators.PAREN_R)
        assertThat(lexer.lex("{")).hasToken(Punctuators.BRACE_L)
        assertThat(lexer.lex("}")).hasToken(Punctuators.BRACE_R)
        assertThat(lexer.lex("=")).hasToken(Punctuators.EQ)
        assertThat(lexer.lex(",")).hasToken(Punctuators.COMMA)
        assertThat(lexer.lex(";")).hasToken(Punctuators.SEMICOLON)
        assertThat(lexer.lex("+")).hasToken(Punctuators.ADD)
        assertThat(lexer.lex("-")).hasToken(Punctuators.SUB)
        assertThat(lexer.lex("*")).hasToken(Punctuators.MUL)
        assertThat(lexer.lex("/")).hasToken(Punctuators.DIV)
        assertThat(lexer.lex("<")).hasToken(Punctuators.LT)
        assertThat(lexer.lex("<=")).hasToken(Punctuators.LTE)
        assertThat(lexer.lex(">")).hasToken(Punctuators.GT)
        assertThat(lexer.lex(">=")).hasToken(Punctuators.GTE)
        assertThat(lexer.lex("==")).hasToken(Punctuators.EQEQ)
        assertThat(lexer.lex("!=")).hasToken(Punctuators.NE)
        assertThat(lexer.lex("++")).hasToken(Punctuators.INC)
        assertThat(lexer.lex("--")).hasToken(Punctuators.DEC)
    }
}