/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
import com.sonar.sslr.test.minic.MiniCParser.parseFile
import com.sonar.sslr.xpath.api.AstNodeXPathQuery.Companion.create
import org.fest.assertions.Assertions
import org.junit.Before
import org.junit.Test

class IdentifiersTooLongTest {
    private lateinit var fileNode: AstNode

    @Before
    fun init() {
        fileNode = parseFile("/xpath/identifiersTooLong.mc")
    }

    @Test
    fun valuesTest() {
        val xpath = create<AstNode>("//IDENTIFIER[string-length(@tokenValue) > 10]")
        val nodes = xpath.selectNodes(fileNode)
        Assertions.assertThat(nodes.size).isEqualTo(3)
        Assertions.assertThat(nodes[0].tokenValue).isEqualTo("aaaaaaaaa11")
        Assertions.assertThat(nodes[0].tokenLine).isEqualTo(3)
        Assertions.assertThat(nodes[1].tokenValue).isEqualTo("bbbbbbbbbbbbb15")
        Assertions.assertThat(nodes[1].tokenLine).isEqualTo(10)
        Assertions.assertThat(nodes[2].tokenValue).isEqualTo("ccccccccc11")
        Assertions.assertThat(nodes[2].tokenLine).isEqualTo(12)
    }

    @Test
    fun noResultValuesTest() {
        val xpath = create<AstNode>("//IDENTIFIER[string-length(@tokenValue) > 50]")
        val nodes = xpath.selectNodes(fileNode)
        Assertions.assertThat(nodes.size).isEqualTo(0)
    }
}