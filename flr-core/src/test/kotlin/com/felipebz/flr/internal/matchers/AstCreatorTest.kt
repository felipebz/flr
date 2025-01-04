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
package com.felipebz.flr.internal.matchers

import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.impl.ast.AstXmlPrinter.Companion.print
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.matchers.AstCreator.Companion.create
import com.felipebz.flr.internal.vm.TokenExpression
import com.felipebz.flr.parser.ParsingResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AstCreatorTest {
    @Test
    fun should_create_tokens_and_trivias() {
        val input = "foo bar".toCharArray()
        val tokenMatcher = mockTokenMatcher(GenericTokenType.IDENTIFIER)
        val triviaMatcher = mockTokenMatcher(GenericTokenType.COMMENT)
        val ruleMatcher = mockRuleMatcher("rule")
        val realAstNodeType = mock<AstNodeType>()
        whenever(ruleMatcher.getRealAstNodeType()).thenReturn(realAstNodeType)
        val triviaNode = ParseNode(0, 4, triviaMatcher)
        val tokenNode = ParseNode(4, 7, tokenMatcher)
        val parseTreeRoot = ParseNode(0, 7, ruleMatcher, arrayOf(triviaNode, tokenNode))
        val inputBuffer: InputBuffer = ImmutableInputBuffer(input)
        val parsingResult = ParsingResult(inputBuffer, true, parseTreeRoot, null)
        val astNode = create(parsingResult, LocatedText(null, input))
        println(print(astNode))
        assertThat(astNode.type).isSameAs(realAstNodeType)
        assertThat(astNode.name).isEqualTo("rule")
        assertThat(astNode.fromIndex).isEqualTo(0)
        assertThat(astNode.toIndex).isEqualTo(7)
        assertThat(astNode.hasChildren()).isTrue()
        assertThat(astNode.tokens).hasSize(1)
        val token = astNode.tokens[0]
        assertThat(astNode.token).isSameAs(token)
        assertThat(token.value).isEqualTo("bar")
        assertThat(token.originalValue).isEqualTo("bar")
        assertThat(token.line).isEqualTo(1)
        assertThat(token.column).isEqualTo(4)
        assertThat(token.type).isEqualTo(GenericTokenType.IDENTIFIER)
        assertThat(token.trivia).hasSize(1)
        val trivia = token.trivia[0]
        val triviaToken = trivia.token
        assertThat(triviaToken.value).isEqualTo("foo ")
        assertThat(triviaToken.originalValue).isEqualTo("foo ")
        assertThat(triviaToken.line).isEqualTo(1)
        assertThat(triviaToken.column).isEqualTo(0)
        assertThat(triviaToken.type).isEqualTo(GenericTokenType.COMMENT)
    }

    @Test
    fun should_create_tokens_without_TokenMatcher() {
        val input = "foobar".toCharArray()
        val firstTerminal = ParseNode(0, 3, null)
        val secondTerminal = ParseNode(3, 6, null)
        val ruleMatcher = mockRuleMatcher("rule")
        val realAstNodeType = mock<AstNodeType>()
        whenever(ruleMatcher.getRealAstNodeType()).thenReturn(realAstNodeType)
        val parseTreeRoot = ParseNode(0, 6, ruleMatcher, arrayOf(firstTerminal, secondTerminal))
        val inputBuffer: InputBuffer = ImmutableInputBuffer(input)
        val parsingResult = ParsingResult(inputBuffer, true, parseTreeRoot, null)
        val astNode = create(parsingResult, LocatedText(null, input))
        println(print(astNode))
        assertThat(astNode.type).isSameAs(realAstNodeType)
        assertThat(astNode.name).isEqualTo("rule")
        assertThat(astNode.fromIndex).isEqualTo(0)
        assertThat(astNode.toIndex).isEqualTo(6)
        assertThat(astNode.hasChildren()).isTrue()
        assertThat(astNode.tokens).hasSize(2)
        var token = astNode.tokens[0]
        assertThat(astNode.token).isSameAs(token)
        assertThat(token.value).isEqualTo("foo")
        assertThat(token.originalValue).isEqualTo("foo")
        assertThat(token.line).isEqualTo(1)
        assertThat(token.column).isEqualTo(0)
        assertThat(token.type).isSameAs(AstCreator.UNDEFINED_TOKEN_TYPE)
        assertThat(token.type.name).isEqualTo("TOKEN")
        token = astNode.tokens[1]
        assertThat(token.value).isEqualTo("bar")
        assertThat(token.originalValue).isEqualTo("bar")
        assertThat(token.line).isEqualTo(1)
        assertThat(token.column).isEqualTo(3)
        assertThat(token.type).isSameAs(AstCreator.UNDEFINED_TOKEN_TYPE)
    }

    @Test
    fun should_skip_nodes() {
        val input = "foo".toCharArray()
        val ruleMatcher1 = mockRuleMatcher("rule1")
        whenever(ruleMatcher1.hasToBeSkippedFromAst(any())).thenReturn(true)
        val ruleMatcher2 = mockRuleMatcher("rule2")
        val realAstNodeType = mock<AstNodeType>()
        whenever(ruleMatcher2.getRealAstNodeType()).thenReturn(realAstNodeType)
        val node = ParseNode(0, 3, ruleMatcher1)
        val parseTreeRoot = ParseNode(0, 3, ruleMatcher2, arrayOf(node))
        val inputBuffer: InputBuffer = ImmutableInputBuffer(input)
        val parsingResult = ParsingResult(inputBuffer, true, parseTreeRoot, null)
        val astNode = create(parsingResult, LocatedText(null, input))
        println(print(astNode))
        assertThat(astNode.type).isSameAs(realAstNodeType)
        assertThat(astNode.name).isEqualTo("rule2")
        assertThat(astNode.fromIndex).isEqualTo(0)
        assertThat(astNode.toIndex).isEqualTo(3)
        assertThat(astNode.hasChildren()).isFalse()
        assertThat(astNode.hasToken()).isFalse()
    }

    companion object {
        private fun mockRuleMatcher(name: String): MutableParsingRule {
            return whenever(mock<MutableParsingRule>().getName()).thenReturn(name).getMock()
        }

        private fun mockTokenMatcher(tokenType: TokenType): TokenExpression {
            return whenever(mock<TokenExpression>().getTokenType()).thenReturn(tokenType)
                .getMock()
        }
    }
}
