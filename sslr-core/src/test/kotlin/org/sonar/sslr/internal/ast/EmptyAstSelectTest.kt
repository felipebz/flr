/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
import com.sonar.sslr.api.AstNodeType
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.sonar.sslr.ast.AstSelect
import org.sonar.sslr.internal.ast.select.EmptyAstSelect
import java.util.*
import java.util.function.Predicate

class EmptyAstSelectTest {
    private val select: AstSelect = EmptyAstSelect()
    @Test
    fun test_children() {
        Assertions.assertThat(select.children() as Any).isSameAs(select)
        Assertions.assertThat(select.children(Mockito.mock(AstNodeType::class.java)) as Any).isSameAs(select)
        Assertions.assertThat(
            select.children(
                Mockito.mock(AstNodeType::class.java), Mockito.mock(
                    AstNodeType::class.java
                )
            ) as Any
        ).isSameAs(select)
    }

    @Test
    fun test_nextSibling() {
        Assertions.assertThat(select.nextSibling() as Any).isSameAs(select)
    }

    @Test
    fun test_previousSibling() {
        Assertions.assertThat(select.previousSibling() as Any).isSameAs(select)
    }

    @Test
    fun test_parent() {
        Assertions.assertThat(select.parent() as Any).isSameAs(select)
    }

    @Test
    fun test_firstAncestor() {
        Assertions.assertThat(select.firstAncestor(Mockito.mock(AstNodeType::class.java)) as Any).isSameAs(select)
        Assertions.assertThat(
            select.firstAncestor(
                Mockito.mock(AstNodeType::class.java), Mockito.mock(
                    AstNodeType::class.java
                )
            ) as Any
        ).isSameAs(select)
    }

    @Test
    fun test_descendants() {
        Assertions.assertThat(select.descendants(Mockito.mock(AstNodeType::class.java)) as Any).isSameAs(select)
        Assertions.assertThat(
            select.descendants(
                Mockito.mock(AstNodeType::class.java), Mockito.mock(
                    AstNodeType::class.java
                )
            ) as Any
        ).isSameAs(select)
    }

    @Test
    fun test_isEmpty() {
        Assertions.assertThat(select.isEmpty()).isTrue()
    }

    @Test
    fun test_isNotEmpty() {
        Assertions.assertThat(select.isNotEmpty()).isFalse()
    }

    @Test
    fun test_filter() {
        Assertions.assertThat(select.filter(Mockito.mock(AstNodeType::class.java)) as Any).isSameAs(select)
        Assertions.assertThat(
            select.filter(
                Mockito.mock(AstNodeType::class.java), Mockito.mock(
                    AstNodeType::class.java
                )
            ) as Any
        ).isSameAs(select)
        Assertions.assertThat(select.filter(mock<Predicate<AstNode>>()) as Any?).isSameAs(select)
    }

    @Test
    fun test_get_non_existing() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            select[0]
        }
    }

    @Test
    fun test_size() {
        Assertions.assertThat(select.size()).isEqualTo(0)
    }

    @Test
    fun test_iterator() {
        Assertions.assertThat(select.iterator() as Any).isSameAs(Collections.emptyIterator<Any>())
    }
}