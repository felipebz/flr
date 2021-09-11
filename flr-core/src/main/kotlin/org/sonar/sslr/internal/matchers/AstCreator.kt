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
package org.sonar.sslr.internal.matchers

import com.sonar.sslr.api.*
import com.sonar.sslr.api.Trivia.TriviaKind
import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.internal.vm.TokenExpression
import org.sonar.sslr.internal.vm.TriviaExpression
import org.sonar.sslr.parser.ParsingResult
import java.net.URI
import java.net.URISyntaxException
import kotlin.math.min

internal class AstCreator private constructor(private val input: LocatedText) {
    companion object {
        private var FAKE_URI: URI
        @JvmStatic
        fun create(parsingResult: ParsingResult, input: LocatedText): AstNode {
            val astNode = AstCreator(input).visit(parsingResult.getParseTreeRoot())
            // Unwrap AstNodeType for root node:
            checkNotNull(astNode).hasToBeSkippedFromAst()
            return astNode
        }

        // @VisibleForTesting
        @JvmField
        val UNDEFINED_TOKEN_TYPE: TokenType = object : TokenType {
            override val name: String
                get() {
                    return "TOKEN"
                }

            override val value: String
                get() {
                    return name
                }

            override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
                return false
            }
        }

        init {
            try {
                FAKE_URI = URI("tests://unittest")
            } catch (e: URISyntaxException) {
                // Can't happen
                throw IllegalStateException(e)
            }
        }
    }

    private val tokenBuilder = Token.builder()
    private val trivias = mutableListOf<Trivia>()
    private fun visit(node: ParseNode): AstNode? {
        return if (node.matcher is MutableParsingRule) {
            visitNonTerminal(node)
        } else {
            visitTerminal(node)
        }
    }

    private fun visitTerminal(node: ParseNode): AstNode? {
        if (node.matcher is TriviaExpression) {
            val ruleMatcher = node.matcher
            return when {
                ruleMatcher.getTriviaKind() == TriviaKind.SKIPPED_TEXT -> {
                    null
                }
                ruleMatcher.getTriviaKind() == TriviaKind.COMMENT -> {
                    updateTokenPositionAndValue(node)
                    tokenBuilder.setTrivia(emptyList())
                    tokenBuilder.setType(GenericTokenType.COMMENT)
                    trivias.add(Trivia.createComment(tokenBuilder.build()))
                    null
                }
                else -> {
                    throw IllegalStateException("Unexpected trivia kind: " + ruleMatcher.getTriviaKind())
                }
            }
        } else if (node.matcher is TokenExpression) {
            updateTokenPositionAndValue(node)
            val ruleMatcher = node.matcher
            tokenBuilder.setType(ruleMatcher.getTokenType())
            if (ruleMatcher.getTokenType() === GenericTokenType.COMMENT) {
                tokenBuilder.setTrivia(emptyList())
                trivias.add(Trivia.createComment(tokenBuilder.build()))
                return null
            }
        } else {
            updateTokenPositionAndValue(node)
            tokenBuilder.setType(UNDEFINED_TOKEN_TYPE)
        }
        val token = tokenBuilder.setTrivia(trivias).build()
        trivias.clear()
        val astNode = AstNode(token)
        astNode.fromIndex = node.startIndex
        astNode.toIndex = node.endIndex
        return astNode
    }

    private fun updateTokenPositionAndValue(node: ParseNode) {
        val location = input.getLocation(node.startIndex)
        tokenBuilder.setGeneratedCode(false)
        tokenBuilder.setLine(location.getLine())
        tokenBuilder.setColumn(location.getColumn() - 1)
        tokenBuilder.setURI(if (location.getFileURI() == null) FAKE_URI else location.getFileURI())
        tokenBuilder.notCopyBook()
        val value = getValue(node)
        tokenBuilder.setValueAndOriginalValue(value)
    }

    private fun visitNonTerminal(node: ParseNode): AstNode {
        val ruleMatcher = node.matcher as MutableParsingRule
        val astNodes = mutableListOf<AstNode>()
        for (child in node.children) {
            val astNode = visit(child)
            if (astNode != null) {
                if (astNode.hasToBeSkippedFromAst()) {
                    astNodes.addAll(astNode.children)
                } else {
                    astNodes.add(astNode)
                }
            }
        }
        var token: Token? = null
        for (child in astNodes) {
            if (child.hasToken()) {
                token = child.token
                break
            }
        }
        val astNode = AstNode(ruleMatcher, ruleMatcher.getName(), token)
        for (child in astNodes) {
            astNode.addChild(child)
        }
        astNode.fromIndex = node.startIndex
        astNode.toIndex = node.endIndex
        return astNode
    }

    private fun getValue(node: ParseNode): String {
        val result = StringBuilder()
        for (i in node.startIndex until min(node.endIndex, input.length)) {
            result.append(input[i])
        }
        return result.toString()
    }
}