/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
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

import com.sonar.sslr.impl.ast.AstXmlPrinter.Companion.print
import org.fest.assertions.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.sonar.sslr.internal.matchers.AstCreator.Companion.create
import org.sonar.sslr.parser.ParseErrorFormatter
import org.sonar.sslr.parser.ParseRunner

class ExpressionGrammarTest {
    private lateinit var grammar: ExpressionGrammar

    @Before
    fun setUp() {
        grammar = ExpressionGrammar()
    }

    @Test
    fun match() {
        val inputString = "20 * ( 2 + 2 ) - var"
        val input = inputString.toCharArray()
        val parseRunner = ParseRunner(grammar.root)
        val result = parseRunner.parse(input)
        assertThat(result.isMatched()).isTrue()
        ParseTreePrinter.print(result.getParseTreeRoot(), input)
        assertThat(ParseTreePrinter.leafsToString(result.getParseTreeRoot(), input)).`as`("full-fidelity")
            .isEqualTo(inputString)
    }

    @Test
    fun mismatch() {
        val inputString = "term +"
        val input = inputString.toCharArray()
        val parseRunner = ParseRunner(grammar.root)
        val result = parseRunner.parse(input)
        assertThat(result.isMatched()).isFalse()
        val parseError = checkNotNull(result.getParseError())
        print(ParseErrorFormatter().format(parseError))
        assertThat(parseError.getErrorIndex()).isEqualTo(6)
    }

    @Test
    fun prefix_match() {
        val inputString = "term +"
        val input = inputString.toCharArray()
        val parseRunner = ParseRunner(grammar.expression)
        val result = parseRunner.parse(input)
        assertThat(result.isMatched()).isTrue()
    }

    @Test
    fun should_mock() {
        val inputString = "term plus term"
        val input = inputString.toCharArray()
        grammar.term.mock()
        grammar.plus.mock()
        val parseRunner = ParseRunner(grammar.root)
        val result = parseRunner.parse(input)
        assertThat(result.isMatched()).isTrue()
        ParseTreePrinter.print(result.getParseTreeRoot(), input)
        assertThat(ParseTreePrinter.leafsToString(result.getParseTreeRoot(), input)).`as`("full-fidelity")
            .isEqualTo(inputString)
    }

    @Test
    @Throws(Exception::class)
    fun should_create_ast() {
        val inputString = "20 * 2 + 2 - var"
        val grammar = ExpressionGrammar()
        val input = inputString.toCharArray()
        val parseRunner = ParseRunner(grammar.root)
        val result = parseRunner.parse(input)
        val astNode = create(result, LocatedText(null, inputString.toCharArray()))
        println(astNode.tokens)
        println(print(astNode))
        assertThat(astNode.tokens).hasSize(7)
        val firstToken = astNode.token
        assertThat(firstToken.line).isEqualTo(1)
        assertThat(firstToken.column).isEqualTo(0)
        assertThat(firstToken.value).isEqualTo("20")
        assertThat(firstToken.originalValue).isEqualTo("20")
        val tokenWithTrivia = checkNotNull(astNode.getFirstDescendant(grammar.mul)).token
        assertThat(tokenWithTrivia.line).isEqualTo(1)
        assertThat(tokenWithTrivia.column).isEqualTo(3)
        assertThat(tokenWithTrivia.trivia).hasSize(1)
        assertThat(tokenWithTrivia.value).isEqualTo("*")
        assertThat(tokenWithTrivia.originalValue).isEqualTo("*")
    }
}