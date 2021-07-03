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
package org.sonar.sslr.internal.ast

import com.sonar.sslr.api.AstNode
import org.fest.assertions.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.sonar.sslr.internal.ast.select.AstSelectFactory.create
import org.sonar.sslr.internal.ast.select.AstSelectFactory.empty
import org.sonar.sslr.internal.ast.select.AstSelectFactory.select
import org.sonar.sslr.internal.ast.select.EmptyAstSelect
import org.sonar.sslr.internal.ast.select.ListAstSelect
import org.sonar.sslr.internal.ast.select.SingleAstSelect

class AstSelectFactoryTest {
    @Test
    fun test_select() {
        assertThat(select(null) as Any).isInstanceOf(
            EmptyAstSelect::class.java
        )
        assertThat(select(mock()) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
    }

    @Test
    fun test_create() {
        val node1 = mock<AstNode>()
        val node2 = mock<AstNode>()
        assertThat(create(listOf()) as Any).isSameAs(empty())
        assertThat(create(listOf(node1)) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        assertThat(create(listOf(node1, node2)) as Any).isInstanceOf(
            ListAstSelect::class.java
        )
    }

    @Test
    fun test_empty() {
        assertThat(empty() as Any).isInstanceOf(EmptyAstSelect::class.java)
        assertThat(empty() as Any).isSameAs(empty())
    }
}