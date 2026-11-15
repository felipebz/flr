//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.parser.ParserAdapter
import com.felipebz.flr.tests.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class ExpressionGrammarTest {
    private val b = ExpressionGrammar.createGrammarBuilder()

    /**
     * This test demonstrates how to use [com.felipebz.flr.tests.Assertions] to test rules of grammar.
     */
    @Test
    fun rules() {
        assertThat(b.build().rule(ExpressionGrammar.EXPRESSION))
            .matches("1 + 1")
            .notMatches("1 +")
            .matches("20 * ( 2 + 2 ) - var")
    }

    /**
     * This test demonstrates how to use [ParserAdapter] to parse and construct AST.
     */
    @Test
    fun ast() {
        val parser = ParserAdapter(StandardCharsets.UTF_8, b.build())
        val rootNode = parser.parse("2 + var")
        assertThat(rootNode.type).isSameAs(ExpressionGrammar.EXPRESSION)
        var astNode = rootNode
        assertThat(astNode.numberOfChildren).isEqualTo(1)
        assertThat(astNode.children[0].type)
            .isSameAs(ExpressionGrammar.ADDITIVE_EXPRESSION)
        astNode = rootNode.children[0]
        assertThat(astNode.numberOfChildren).isEqualTo(3)
        assertThat(astNode.children[0].type).isSameAs(ExpressionGrammar.NUMBER)
        assertThat(astNode.children[1].type).isSameAs(ExpressionGrammar.PLUS)
        assertThat(astNode.children[2].type).isSameAs(ExpressionGrammar.VARIABLE)
    }
}
