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
package org.sonar.sslr.examples.grammars

import org.junit.Test
import org.sonar.sslr.parser.ParserAdapter
import org.sonar.sslr.tests.Assertions.assertThat
import org.fest.assertions.Assertions.assertThat
import java.nio.charset.StandardCharsets

class ExpressionGrammarTest {
    private val b = ExpressionGrammar.createGrammarBuilder()

    /**
     * This test demonstrates how to use [org.sonar.sslr.tests.Assertions] to test rules of grammar.
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