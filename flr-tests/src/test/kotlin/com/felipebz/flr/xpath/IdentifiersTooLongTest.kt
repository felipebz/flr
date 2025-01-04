/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
