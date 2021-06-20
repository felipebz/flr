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

import com.sonar.sslr.api.AstNodeType
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.ast.AstXmlPrinter.Companion.print
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.internal.matchers.AstCreator.Companion.create
import org.sonar.sslr.internal.vm.TokenExpression
import org.sonar.sslr.parser.ParsingResult

class AstCreatorTest {
    @Test
    fun should_create_tokens_and_trivias() {
        val input = "foo bar".toCharArray()
        val tokenMatcher = mockTokenMatcher(GenericTokenType.IDENTIFIER)
        val triviaMatcher = mockTokenMatcher(GenericTokenType.COMMENT)
        val ruleMatcher = mockRuleMatcher("rule")
        val realAstNodeType = Mockito.mock(AstNodeType::class.java)
        Mockito.`when`(ruleMatcher.getRealAstNodeType()).thenReturn(realAstNodeType)
        val triviaNode = ParseNode(0, 4, emptyList(), triviaMatcher)
        val tokenNode = ParseNode(4, 7, emptyList(), tokenMatcher)
        val parseTreeRoot = ParseNode(0, 7, listOf(triviaNode, tokenNode), ruleMatcher)
        val inputBuffer: InputBuffer = ImmutableInputBuffer(input)
        val parsingResult = ParsingResult(inputBuffer, true, parseTreeRoot, null)
        val astNode = create(parsingResult, LocatedText(null, input))
        println(print(astNode))
        Assertions.assertThat(astNode.type).isSameAs(realAstNodeType)
        Assertions.assertThat(astNode.name).isEqualTo("rule")
        Assertions.assertThat(astNode.fromIndex).isEqualTo(0)
        Assertions.assertThat(astNode.toIndex).isEqualTo(7)
        Assertions.assertThat(astNode.hasChildren()).isTrue()
        Assertions.assertThat(astNode.tokens).hasSize(1)
        val token = astNode.tokens[0]
        Assertions.assertThat(astNode.token).isSameAs(token)
        Assertions.assertThat(token.value).isEqualTo("bar")
        Assertions.assertThat(token.originalValue).isEqualTo("bar")
        Assertions.assertThat(token.line).isEqualTo(1)
        Assertions.assertThat(token.column).isEqualTo(4)
        Assertions.assertThat(token.type).isEqualTo(GenericTokenType.IDENTIFIER)
        Assertions.assertThat(token.trivia).hasSize(1)
        val trivia = token.trivia[0]
        val triviaToken = trivia.token
        Assertions.assertThat(triviaToken.value).isEqualTo("foo ")
        Assertions.assertThat(triviaToken.originalValue).isEqualTo("foo ")
        Assertions.assertThat(triviaToken.line).isEqualTo(1)
        Assertions.assertThat(triviaToken.column).isEqualTo(0)
        Assertions.assertThat(triviaToken.type).isEqualTo(GenericTokenType.COMMENT)
    }

    @Test
    fun should_create_tokens_without_TokenMatcher() {
        val input = "foobar".toCharArray()
        val firstTerminal = ParseNode(0, 3, emptyList(), null)
        val secondTerminal = ParseNode(3, 6, emptyList(), null)
        val ruleMatcher = mockRuleMatcher("rule")
        val realAstNodeType = Mockito.mock(AstNodeType::class.java)
        Mockito.`when`(ruleMatcher.getRealAstNodeType()).thenReturn(realAstNodeType)
        val parseTreeRoot = ParseNode(0, 6, listOf(firstTerminal, secondTerminal), ruleMatcher)
        val inputBuffer: InputBuffer = ImmutableInputBuffer(input)
        val parsingResult = ParsingResult(inputBuffer, true, parseTreeRoot, null)
        val astNode = create(parsingResult, LocatedText(null, input))
        println(print(astNode))
        Assertions.assertThat(astNode.type).isSameAs(realAstNodeType)
        Assertions.assertThat(astNode.name).isEqualTo("rule")
        Assertions.assertThat(astNode.fromIndex).isEqualTo(0)
        Assertions.assertThat(astNode.toIndex).isEqualTo(6)
        Assertions.assertThat(astNode.hasChildren()).isTrue()
        Assertions.assertThat(astNode.tokens).hasSize(2)
        var token = astNode.tokens[0]
        Assertions.assertThat(astNode.token).isSameAs(token)
        Assertions.assertThat(token.value).isEqualTo("foo")
        Assertions.assertThat(token.originalValue).isEqualTo("foo")
        Assertions.assertThat(token.line).isEqualTo(1)
        Assertions.assertThat(token.column).isEqualTo(0)
        Assertions.assertThat(token.type).isSameAs(AstCreator.UNDEFINED_TOKEN_TYPE)
        Assertions.assertThat(token.type.name).isEqualTo("TOKEN")
        token = astNode.tokens[1]
        Assertions.assertThat(token.value).isEqualTo("bar")
        Assertions.assertThat(token.originalValue).isEqualTo("bar")
        Assertions.assertThat(token.line).isEqualTo(1)
        Assertions.assertThat(token.column).isEqualTo(3)
        Assertions.assertThat(token.type).isSameAs(AstCreator.UNDEFINED_TOKEN_TYPE)
    }

    @Test
    fun should_skip_nodes() {
        val input = "foo".toCharArray()
        val ruleMatcher1 = mockRuleMatcher("rule1")
        Mockito.`when`(ruleMatcher1.hasToBeSkippedFromAst(any())).thenReturn(true)
        val ruleMatcher2 = mockRuleMatcher("rule2")
        val realAstNodeType = Mockito.mock(AstNodeType::class.java)
        Mockito.`when`(ruleMatcher2.getRealAstNodeType()).thenReturn(realAstNodeType)
        val node = ParseNode(0, 3, emptyList(), ruleMatcher1)
        val parseTreeRoot = ParseNode(0, 3, listOf(node), ruleMatcher2)
        val inputBuffer: InputBuffer = ImmutableInputBuffer(input)
        val parsingResult = ParsingResult(inputBuffer, true, parseTreeRoot, null)
        val astNode = create(parsingResult, LocatedText(null, input))
        println(print(astNode))
        Assertions.assertThat(astNode.type).isSameAs(realAstNodeType)
        Assertions.assertThat(astNode.name).isEqualTo("rule2")
        Assertions.assertThat(astNode.fromIndex).isEqualTo(0)
        Assertions.assertThat(astNode.toIndex).isEqualTo(3)
        Assertions.assertThat(astNode.hasChildren()).isFalse()
        Assertions.assertThat(astNode.hasToken()).isFalse()
    }

    companion object {
        private fun mockRuleMatcher(name: String): MutableParsingRule {
            return Mockito.`when`(Mockito.mock(MutableParsingRule::class.java).getName()).thenReturn(name).getMock()
        }

        private fun mockTokenMatcher(tokenType: TokenType): TokenExpression {
            return Mockito.`when`(Mockito.mock(TokenExpression::class.java).getTokenType()).thenReturn(tokenType)
                .getMock()
        }
    }
}