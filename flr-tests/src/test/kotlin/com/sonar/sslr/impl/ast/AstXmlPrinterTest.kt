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
package com.sonar.sslr.impl.ast

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.ast.AstXmlPrinter.Companion.print
import com.sonar.sslr.impl.matcher.RuleDefinition
import com.sonar.sslr.test.lexer.MockHelper.mockToken
import com.sonar.sslr.test.lexer.MockHelper.mockTokenBuilder
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test

class AstXmlPrinterTest {
    @Test
    fun testPrintRuleAstNode() {
        val root = AstNode(
            RuleDefinition("expr"), "expr", mockTokenBuilder(WordTokenType(), "word").setLine(34)
                .setColumn(12).build()
        )
        assertThat(print(root)).isEqualTo("<expr tokenValue=\"word\" tokenLine=\"34\" tokenColumn=\"12\"/>")
    }

    @Test
    fun testPrintWordAstNode() {
        val root = AstNode(mockToken(WordTokenType(), "myword"))
        assertThat(print(root)).isEqualTo("<WORD tokenValue=\"myword\" tokenLine=\"1\" tokenColumn=\"1\"/>")
    }

    @Test
    fun testPrintFullAstNode() {
        val astNode = AstNode(RuleDefinition("expr"), "expr", null)
        astNode.addChild(AstNode(mockToken(WordTokenType(), "x")))
        astNode.addChild(AstNode(mockToken(WordTokenType(), "=")))
        astNode.addChild(AstNode(mockToken(WordTokenType(), "4")))
        astNode.addChild(AstNode(mockToken(WordTokenType(), "WORD")))
        val expectedResult = StringBuilder()
            .append("<expr>\n")
            .append("  <WORD tokenValue=\"x\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
            .append("  <WORD tokenValue=\"=\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
            .append("  <WORD tokenValue=\"4\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
            .append("  <WORD tokenValue=\"WORD\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
            .append("</expr>")
            .toString()
        assertThat(print(astNode)).isEqualTo(expectedResult)
    }

    private class WordTokenType : TokenType {
        override val name: String
            get() = "WORD"

        override fun hasToBeSkippedFromAst(node: AstNode?): Boolean {
            return false
        }

        override val value: String
            get() = "WORDS"
    }
}