//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.xpath

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.test.minic.MiniCParser.parseFile
import com.felipebz.flr.xpath.api.AstNodeXPathQuery.Companion.create
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdentifiersTooLongTest {
    private lateinit var fileNode: AstNode

    @BeforeEach
    fun init() {
        fileNode = parseFile("/xpath/identifiersTooLong.mc")
    }

    @Test
    fun valuesTest() {
        val xpath = create<AstNode>("//IDENTIFIER[string-length(@tokenValue) > 10]")
        val nodes = xpath.selectNodes(fileNode)
        assertThat(nodes.size).isEqualTo(3)
        assertThat(nodes[0].tokenValue).isEqualTo("aaaaaaaaa11")
        assertThat(nodes[0].tokenLine).isEqualTo(3)
        assertThat(nodes[1].tokenValue).isEqualTo("bbbbbbbbbbbbb15")
        assertThat(nodes[1].tokenLine).isEqualTo(10)
        assertThat(nodes[2].tokenValue).isEqualTo("ccccccccc11")
        assertThat(nodes[2].tokenLine).isEqualTo(12)
    }

    @Test
    fun noResultValuesTest() {
        val xpath = create<AstNode>("//IDENTIFIER[string-length(@tokenValue) > 50]")
        val nodes = xpath.selectNodes(fileNode)
        assertThat(nodes.size).isEqualTo(0)
    }
}
