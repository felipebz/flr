/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.internal.matchers

import com.sonar.sslr.api.*
import org.sonar.sslr.parser.GrammarOperators.commentTrivia
import org.sonar.sslr.parser.GrammarOperators.endOfInput
import org.sonar.sslr.parser.GrammarOperators.firstOf
import org.sonar.sslr.parser.GrammarOperators.regexp
import org.sonar.sslr.parser.GrammarOperators.zeroOrMore
import org.sonar.sslr.parser.LexerlessGrammar

class ExpressionGrammar : LexerlessGrammar() {
    lateinit var whitespace: Rule
    lateinit var endOfInput: Rule
    lateinit var plus: Rule
    lateinit var minus: Rule
    lateinit var div: Rule
    lateinit var mul: Rule
    lateinit var number: Rule
    lateinit var variable: Rule
    lateinit var lpar: Rule
    lateinit var rpar: Rule
    lateinit var root: Rule
    lateinit var expression: Rule
    lateinit var term: Rule
    lateinit var factor: Rule
    lateinit var parens: Rule

    override fun getRootRule(): Rule {
        return root
    }

    init {
        whitespace.`is`(commentTrivia(regexp("\\s*+"))).skip()
        plus.`is`('+', whitespace)
        minus.`is`('-', whitespace)
        div.`is`('/', whitespace)
        mul.`is`('*', whitespace)
        number.`is`(regexp("[0-9]++"), whitespace)
        variable.`is`(regexp("\\p{javaJavaIdentifierStart}++\\p{javaJavaIdentifierPart}*+"), whitespace)
        lpar.`is`('(', whitespace)
        rpar.`is`(')', whitespace)
        endOfInput.`is`(endOfInput())

        // If in part of grammar below we will replace
        // plus, minus, div, mul, lpar and rpar by punctuators '+', '-', '/', '*', '(' and ')' respectively,
        // number by GenericTokenType.CONSTANT, variable by GenericTokenType.IDENTIFIER
        // and remove space
        // then it will look exactly as it was with lexer:
        root.`is`(whitespace, expression, endOfInput)
        expression.`is`(term, zeroOrMore(firstOf(plus, minus), term))
        term.`is`(factor, zeroOrMore(firstOf(div, mul), factor))
        factor.`is`(firstOf(number, parens, variable))
        parens.`is`(lpar, expression, rpar)
    }
}