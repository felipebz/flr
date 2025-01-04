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
package com.felipebz.flr.test.minic

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Grammar
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.test.minic.MiniCLexer.Literals
import com.felipebz.flr.test.minic.MiniCLexer.Punctuators

public enum class MiniCGrammar : GrammarRuleKey {
    BIN_TYPE,
    BIN_FUNCTION_DEFINITION,
    BIN_PARAMETER,
    BIN_VARIABLE_DEFINITION,
    BIN_FUNCTION_REFERENCE,
    BIN_VARIABLE_REFERENCE,
    COMPILATION_UNIT,
    DEFINITION,
    STRUCT_DEFINITION,
    STRUCT_MEMBER,
    FUNCTION_DEFINITION,
    VARIABLE_DEFINITION,
    PARAMETERS_LIST,
    PARAMETER_DECLARATION,
    COMPOUND_STATEMENT,
    VARIABLE_INITIALIZER,
    ARGUMENT_EXPRESSION_LIST,
    STATEMENT,
    EXPRESSION_STATEMENT,
    RETURN_STATEMENT,
    CONTINUE_STATEMENT,
    BREAK_STATEMENT,
    IF_STATEMENT,
    WHILE_STATEMENT,
    CONDITION_CLAUSE,
    ELSE_CLAUSE,
    NO_COMPLEXITY_STATEMENT,
    EXPRESSION,
    ASSIGNMENT_EXPRESSION,
    RELATIONAL_EXPRESSION,
    RELATIONAL_OPERATOR,
    ADDITIVE_EXPRESSION,
    ADDITIVE_OPERATOR,
    MULTIPLICATIVE_EXPRESSION,
    MULTIPLICATIVE_OPERATOR,
    UNARY_EXPRESSION,
    UNARY_OPERATOR,
    POSTFIX_EXPRESSION,
    POSTFIX_OPERATOR,
    PRIMARY_EXPRESSION;

    public companion object {
        public fun create(): Grammar {
            val b = LexerfulGrammarBuilder.create()

            // Bins
            b.rule(BIN_TYPE).`is`(b.firstOf(MiniCLexer.Keywords.INT, MiniCLexer.Keywords.VOID))
            b.rule(BIN_PARAMETER).`is`(GenericTokenType.IDENTIFIER)
            b.rule(BIN_FUNCTION_DEFINITION).`is`(GenericTokenType.IDENTIFIER)
            b.rule(BIN_VARIABLE_DEFINITION).`is`(GenericTokenType.IDENTIFIER)
            b.rule(BIN_FUNCTION_REFERENCE).`is`(GenericTokenType.IDENTIFIER)
            b.rule(BIN_VARIABLE_REFERENCE).`is`(GenericTokenType.IDENTIFIER)

            // Miscellaneous
            b.rule(COMPILATION_UNIT).`is`(b.zeroOrMore(DEFINITION), GenericTokenType.EOF)
            b.rule(DEFINITION).`is`(b.firstOf(STRUCT_DEFINITION, FUNCTION_DEFINITION, VARIABLE_DEFINITION))
            b.rule(STRUCT_DEFINITION).`is`(
                MiniCLexer.Keywords.STRUCT, GenericTokenType.IDENTIFIER, Punctuators.BRACE_L, b.oneOrMore(
                    STRUCT_MEMBER, Punctuators.SEMICOLON
                ), Punctuators.BRACE_R
            )
            b.rule(STRUCT_MEMBER).`is`(BIN_TYPE, GenericTokenType.IDENTIFIER)
            b.rule(FUNCTION_DEFINITION).`is`(
                BIN_TYPE, BIN_FUNCTION_DEFINITION, Punctuators.PAREN_L, b.optional(
                    PARAMETERS_LIST
                ), Punctuators.PAREN_R, COMPOUND_STATEMENT
            )
            b.rule(VARIABLE_DEFINITION)
                .`is`(BIN_TYPE, BIN_VARIABLE_DEFINITION, b.optional(VARIABLE_INITIALIZER), Punctuators.SEMICOLON)
            b.rule(PARAMETERS_LIST).`is`(PARAMETER_DECLARATION, b.zeroOrMore(Punctuators.COMMA, PARAMETER_DECLARATION))
            b.rule(PARAMETER_DECLARATION).`is`(BIN_TYPE, BIN_PARAMETER)
            b.rule(COMPOUND_STATEMENT).`is`(
                Punctuators.BRACE_L, b.zeroOrMore(VARIABLE_DEFINITION), b.zeroOrMore(
                    STATEMENT
                ), Punctuators.BRACE_R
            )
            b.rule(VARIABLE_INITIALIZER).`is`(Punctuators.EQ, EXPRESSION)
            b.rule(ARGUMENT_EXPRESSION_LIST).`is`(EXPRESSION, b.zeroOrMore(Punctuators.COMMA, EXPRESSION))

            // Statements
            b.rule(STATEMENT).`is`(
                b.firstOf(
                    EXPRESSION_STATEMENT,
                    COMPOUND_STATEMENT,
                    RETURN_STATEMENT,
                    CONTINUE_STATEMENT,
                    BREAK_STATEMENT,
                    IF_STATEMENT,
                    WHILE_STATEMENT,
                    NO_COMPLEXITY_STATEMENT
                )
            )
            b.rule(EXPRESSION_STATEMENT).`is`(EXPRESSION, Punctuators.SEMICOLON)
            b.rule(RETURN_STATEMENT).`is`(MiniCLexer.Keywords.RETURN, EXPRESSION, Punctuators.SEMICOLON)
            b.rule(CONTINUE_STATEMENT).`is`(MiniCLexer.Keywords.CONTINUE, Punctuators.SEMICOLON)
            b.rule(BREAK_STATEMENT).`is`(MiniCLexer.Keywords.BREAK, Punctuators.SEMICOLON)
            b.rule(IF_STATEMENT).`is`(MiniCLexer.Keywords.IF, CONDITION_CLAUSE, STATEMENT, b.optional(ELSE_CLAUSE))
            b.rule(WHILE_STATEMENT).`is`(MiniCLexer.Keywords.WHILE, CONDITION_CLAUSE, STATEMENT)
            b.rule(CONDITION_CLAUSE).`is`(Punctuators.PAREN_L, EXPRESSION, Punctuators.PAREN_R)
            b.rule(ELSE_CLAUSE).`is`(MiniCLexer.Keywords.ELSE, STATEMENT)
            b.rule(NO_COMPLEXITY_STATEMENT).`is`("nocomplexity", STATEMENT)

            // Expressions
            b.rule(EXPRESSION).`is`(ASSIGNMENT_EXPRESSION)
            b.rule(ASSIGNMENT_EXPRESSION).`is`(RELATIONAL_EXPRESSION, b.optional(Punctuators.EQ, RELATIONAL_EXPRESSION))
                .skipIfOneChild()
            b.rule(RELATIONAL_EXPRESSION)
                .`is`(ADDITIVE_EXPRESSION, b.optional(RELATIONAL_OPERATOR, RELATIONAL_EXPRESSION)).skipIfOneChild()
            b.rule(RELATIONAL_OPERATOR).`is`(
                b.firstOf(
                    Punctuators.EQEQ,
                    Punctuators.NE,
                    Punctuators.LT,
                    Punctuators.LTE,
                    Punctuators.GT,
                    Punctuators.GTE
                )
            )
            b.rule(ADDITIVE_EXPRESSION)
                .`is`(MULTIPLICATIVE_EXPRESSION, b.optional(ADDITIVE_OPERATOR, ADDITIVE_EXPRESSION)).skipIfOneChild()
            b.rule(ADDITIVE_OPERATOR).`is`(b.firstOf(Punctuators.ADD, Punctuators.SUB))
            b.rule(MULTIPLICATIVE_EXPRESSION)
                .`is`(UNARY_EXPRESSION, b.optional(MULTIPLICATIVE_OPERATOR, MULTIPLICATIVE_EXPRESSION)).skipIfOneChild()
            b.rule(MULTIPLICATIVE_OPERATOR).`is`(b.firstOf(Punctuators.MUL, Punctuators.DIV))
            b.rule(UNARY_EXPRESSION).`is`(
                b.firstOf(
                    b.sequence(UNARY_OPERATOR, PRIMARY_EXPRESSION), POSTFIX_EXPRESSION
                )
            ).skipIfOneChild()
            b.rule(UNARY_OPERATOR).`is`(b.firstOf(Punctuators.INC, Punctuators.DEC))
            b.rule(POSTFIX_EXPRESSION).`is`(
                b.firstOf(
                    b.sequence(PRIMARY_EXPRESSION, POSTFIX_OPERATOR),
                    b.sequence(
                        BIN_FUNCTION_REFERENCE,
                        Punctuators.PAREN_L,
                        b.optional(ARGUMENT_EXPRESSION_LIST),
                        Punctuators.PAREN_R
                    ), PRIMARY_EXPRESSION
                )
            ).skipIfOneChild()
            b.rule(POSTFIX_OPERATOR).`is`(b.firstOf(Punctuators.INC, Punctuators.DEC))
            b.rule(PRIMARY_EXPRESSION).`is`(
                b.firstOf(
                    Literals.INTEGER, BIN_VARIABLE_REFERENCE,
                    b.sequence(Punctuators.PAREN_L, EXPRESSION, Punctuators.PAREN_R)
                )
            )
            b.setRootRule(COMPILATION_UNIT)
            return b.build()
        }
    }
}
