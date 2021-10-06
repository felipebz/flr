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
package com.felipebz.flr.xpath

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.test.minic.MiniCGrammar
import com.felipebz.flr.test.minic.MiniCParser.parseFile
import com.felipebz.flr.xpath.api.AstNodeXPathQuery.Companion.create
import org.fest.assertions.Assertions.assertThat
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