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
package com.sonar.sslr.xpath

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.test.minic.MiniCGrammar
import com.sonar.sslr.test.minic.MiniCParser.parseFile
import com.sonar.sslr.xpath.api.AstNodeXPathQuery.Companion.create
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BasicQueriesTest {
    private lateinit var fileNode: AstNode

    @BeforeEach
    fun init() {
        fileNode = parseFile("/xpath/basicQueries.mc")
    }

    @Test
    fun compilationUnitTest() {
        val xpath = create<AstNode>("/COMPILATION_UNIT")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode)
    }

    @Test
    fun anyCompilationUnitTest() {
        val xpath = create<AstNode>("//COMPILATION_UNIT")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode)
    }

    @Test
    fun compilationUnitWithPredicateWithEOFTest() {
        val xpath = create<AstNode>("/COMPILATION_UNIT[not(not(EOF))]")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode)
    }

    @Test
    fun compilationUnitWithPredicateWithoutEOFTest() {
        val xpath = create<AstNode>("/COMPILATION_UNIT[not(EOF)]")
        assertThat(xpath.selectSingleNode(fileNode)).isNull()
    }

    @Test
    fun EOFTest() {
        val xpath = create<AstNode>("/COMPILATION_UNIT/EOF")
        assertThat(xpath.selectSingleNode(fileNode))
            .isEqualTo(fileNode.getFirstChild(GenericTokenType.EOF))
    }

    @Test
    fun anyEOFTest() {
        val xpath = create<AstNode>("//EOF")
        assertThat(xpath.selectSingleNode(fileNode))
            .isEqualTo(fileNode.getFirstChild(GenericTokenType.EOF))
    }

    @Test
    fun getTokenValueAttributeTest() {
        val xpath = create<String>("string(/COMPILATION_UNIT/@tokenValue)")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo("int")
    }

    @Test
    fun getTokenLineAttributeTest() {
        val xpath = create<String>("string(/COMPILATION_UNIT/@tokenLine)")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo("2")
    }

    @Test
    fun getTokenColumnAttributeTest() {
        val xpath = create<String>("string(/COMPILATION_UNIT/@tokenColumn)")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo("0")
    }

    @Test
    fun getSecondDeclarationTest() {
        val xpath1 = create<AstNode>("/COMPILATION_UNIT/DEFINITION[@tokenLine=4]")
        val xpath2 = create<AstNode>("/COMPILATION_UNIT/DEFINITION[2]")
        val declarationAtLineFour = fileNode.children[1]
        assertThat(declarationAtLineFour.`is`(MiniCGrammar.DEFINITION)).isTrue()
        assertThat(declarationAtLineFour.tokenLine).isEqualTo(4)
        assertThat(xpath1.selectSingleNode(fileNode)).isEqualTo(declarationAtLineFour)
        assertThat(xpath1.selectSingleNode(fileNode)).isEqualTo(
            xpath2.selectSingleNode(
                fileNode
            )
        )
    }

    @Test
    fun identifiersCountTest() {
        val xpath = create<AstNode>("/COMPILATION_UNIT[count(//IDENTIFIER) = 2]")
        assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode)
    }

    @Test
    fun getIdentifiersTest() {
        val xpath = create<AstNode>("//IDENTIFIER")
        val nodes = xpath.selectNodes(fileNode)
        assertThat(nodes.size).isEqualTo(2)
        assertThat(nodes[0].tokenValue).isEqualTo("a")
        assertThat(nodes[1].tokenValue).isEqualTo("b")
    }
}