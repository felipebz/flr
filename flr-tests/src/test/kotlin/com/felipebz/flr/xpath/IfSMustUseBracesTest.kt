//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.xpath

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.test.minic.MiniCParser.parseFile
import com.felipebz.flr.xpath.api.AstNodeXPathQuery.Companion.create
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IfSMustUseBracesTest {
    private lateinit var fileNode: AstNode

    @BeforeEach
    fun init() {
        fileNode = parseFile("/xpath/ifSMustUseBraces.mc")
    }

    @Test
    fun firstValueEqualsOnlyValueTest() {
        val xpath = create<AstNode>(
            "//IF_STATEMENT/STATEMENT[not(COMPOUND_STATEMENT)]/..|//ELSE_CLAUSE/STATEMENT[not(COMPOUND_STATEMENT)]/.."
        )
        val nodes = xpath.selectNodes(fileNode)
        assertThat(nodes.size).isEqualTo(2)
        assertThat(nodes[0]).isEqualTo(xpath.selectSingleNode(fileNode))
    }

    @Test
    fun valuesTest() {
        val xpath = create<AstNode>(
            "//IF_STATEMENT/STATEMENT[not(COMPOUND_STATEMENT)]/..|//ELSE_CLAUSE/STATEMENT[not(COMPOUND_STATEMENT)]/.."
        )
        val nodes = xpath.selectNodes(fileNode)
        assertThat(nodes.size).isEqualTo(2)
        assertThat(nodes[0].`is`(MiniCGrammar.IF_STATEMENT)).isTrue()
        assertThat(nodes[0].tokenLine).isEqualTo(3)
        assertThat(nodes[1].`is`(MiniCGrammar.ELSE_CLAUSE)).isTrue()
        assertThat(nodes[1].tokenLine).isEqualTo(16)
    }
}
