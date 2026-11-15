//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.ast

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.TokenType
import com.felipebz.flr.impl.ast.AstXmlPrinter.Companion.print
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.test.testToken
import com.felipebz.flr.test.testTokenBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AstXmlPrinterTest {
    @Test
    fun testPrintRuleAstNode() {
        val root = AstNode(
            RuleDefinition("expr"), "expr", testTokenBuilder(WordTokenType(), "word").setLine(34)
                .setColumn(12).build()
        )
        assertThat(print(root)).isEqualTo("<expr tokenValue=\"word\" tokenLine=\"34\" tokenColumn=\"12\"/>")
    }

    @Test
    fun testPrintWordAstNode() {
        val root = AstNode(testToken(WordTokenType(), "myword"))
        assertThat(print(root)).isEqualTo("<WORD tokenValue=\"myword\" tokenLine=\"1\" tokenColumn=\"1\"/>")
    }

    @Test
    fun testPrintFullAstNode() {
        val astNode = AstNode(RuleDefinition("expr"), "expr", null)
        astNode.addChild(AstNode(testToken(WordTokenType(), "x")))
        astNode.addChild(AstNode(testToken(WordTokenType(), "=")))
        astNode.addChild(AstNode(testToken(WordTokenType(), "4")))
        astNode.addChild(AstNode(testToken(WordTokenType(), "WORD")))
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
