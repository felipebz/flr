//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.xpath

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.test.minic.MiniCParser.parseFile
import com.felipebz.flr.xpath.api.AstNodeXPathQuery.Companion.create
import org.assertj.core.api.Assertions.assertThat
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
