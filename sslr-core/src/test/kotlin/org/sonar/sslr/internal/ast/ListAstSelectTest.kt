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
import com.sonar.sslr.api.AstNodeType
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.sonar.sslr.internal.ast.select.AstSelectFactory.empty
import org.sonar.sslr.internal.ast.select.ListAstSelect
import org.sonar.sslr.internal.ast.select.SingleAstSelect
import java.util.function.Predicate

class ListAstSelectTest {
    private lateinit var node1: AstNode
    private lateinit var node2: AstNode
    private lateinit var select: ListAstSelect
    @Before
    fun init() {
        node1 = mock()
        node2 = mock()
        select = ListAstSelect(listOf(node1, node2))
    }

    @Test
    fun test_children_when_no_children() {
        assertThat(select.children() as Any).isSameAs(empty())
        assertThat(select.children(mock()) as Any).isSameAs(empty())
        assertThat(select.children(mock(), mock()) as Any).isSameAs(empty())
    }

    @Test
    fun test_children_when_one_child() {
        val type1 = mock<AstNodeType>()
        val type2 = mock<AstNodeType>()
        val child = mock<AstNode>()
        whenever<List<AstNode>>(node1.children).thenReturn(listOf(child))
        whenever(node1.firstChild).thenReturn(child)
        var children = select.children()
        assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(children).containsOnly(child)
        whenever<List<AstNode>>(node1.children).thenReturn(listOf(child))
        children = select.children(type1)
        assertThat(children as Any).isSameAs(empty())
        whenever(child.type).thenReturn(type1)
        children = select.children(type1)
        assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(children).containsOnly(child)
        children = select.children(type1, type2)
        assertThat(children as Any).isSameAs(empty())
        whenever(child.`is`(type1, type2)).thenReturn(true)
        children = select.children(type1, type2)
        assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(children).containsOnly(child)
    }

    @Test
    fun test_chilren_when_more_than_one_child() {
        val type1 = mock<AstNodeType>()
        val type2 = mock<AstNodeType>()
        val child1 = mock<AstNode>()
        val child2 = mock<AstNode>()
        whenever<List<AstNode>>(node1.children).thenReturn(listOf(child1))
        whenever<List<AstNode>>(node2.children).thenReturn(listOf(child2))
        var children = select.children()
        assertThat(children as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(children).containsOnly(child1, child2)
        children = select.children(type1)
        assertThat(children as Any).isSameAs(empty())
        whenever(child1.type).thenReturn(type1)
        children = select.children(type1)
        assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(children).containsOnly(child1)
        whenever(child2.type).thenReturn(type1)
        children = select.children(type1)
        assertThat(children as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(children).containsOnly(child1, child2)
        children = select.children(type1, type2)
        assertThat(children as Any).isSameAs(empty())
        whenever(child1.`is`(type1, type2)).thenReturn(true)
        children = select.children(type1, type2)
        assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(children).containsOnly(child1)
        whenever(child2.`is`(type1, type2)).thenReturn(true)
        children = select.children(type1, type2)
        assertThat(children as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(children).containsOnly(child1, child2)
    }

    @Test
    fun test_nextSibling() {
        assertThat(select.nextSibling() as Any).isSameAs(empty())
        val sibling1 = mock<AstNode>()
        whenever(node1.nextSibling).thenReturn(sibling1)
        assertThat(select.nextSibling() as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(select.nextSibling()).containsOnly(sibling1)
        val sibling2 = mock<AstNode>()
        whenever(node2.nextSibling).thenReturn(sibling2)
        assertThat(select.nextSibling() as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.nextSibling()).containsOnly(sibling1, sibling2)
    }

    @Test
    fun test_previousSibling() {
        assertThat(select.previousSibling() as Any).isSameAs(empty())
        val sibling1 = mock<AstNode>()
        whenever(node1.previousSibling).thenReturn(sibling1)
        assertThat(select.previousSibling() as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(select.previousSibling()).containsOnly(sibling1)
        val sibling2 = mock<AstNode>()
        whenever(node2.previousSibling).thenReturn(sibling2)
        assertThat(select.previousSibling() as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.previousSibling()).containsOnly(sibling1, sibling2)
    }

    @Test
    fun test_parent() {
        assertThat(select.parent() as Any).isSameAs(empty())
        val parent1 = mock<AstNode>()
        whenever(node1.parent).thenReturn(parent1)
        assertThat(select.parent() as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(select.parent()).containsOnly(parent1)
        val parent2 = mock<AstNode>()
        whenever(node2.parent).thenReturn(parent2)
        assertThat(select.parent() as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.parent()).containsOnly(parent1, parent2)
    }

    @Test
    fun test_firstAncestor_by_type() {
        val type = mock<AstNodeType>()
        assertThat(select.firstAncestor(type) as Any).isSameAs(empty())
        val parent = mock<AstNode>()
        whenever(node1.parent).thenReturn(parent)
        val ancestor1 = mock<AstNode>()
        whenever(ancestor1.type).thenReturn(type)
        whenever(parent.parent).thenReturn(ancestor1)
        assertThat(select.firstAncestor(type) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        assertThat(select.firstAncestor(type)).containsOnly(ancestor1)
        val ancestor2 = mock<AstNode>()
        whenever(ancestor2.type).thenReturn(type)
        whenever(node2.parent).thenReturn(ancestor2)
        assertThat(select.firstAncestor(type) as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.firstAncestor(type)).containsOnly(ancestor1, ancestor2)
    }

    @Test
    fun test_firstAncestor_by_types() {
        val type1 = mock<AstNodeType>()
        val type2 = mock<AstNodeType>()
        assertThat(select.firstAncestor(type1, type2) as Any).isSameAs(empty())
        val parent = mock<AstNode>()
        whenever(node1.parent).thenReturn(parent)
        val ancestor1 = mock<AstNode>()
        whenever(ancestor1.`is`(type1, type2)).thenReturn(true)
        whenever(parent.parent).thenReturn(ancestor1)
        assertThat(select.firstAncestor(type1, type2) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        assertThat(select.firstAncestor(type1, type2)).containsOnly(ancestor1)
        val ancestor2 = mock<AstNode>()
        whenever(ancestor2.`is`(type1, type2)).thenReturn(true)
        whenever(node2.parent).thenReturn(ancestor2)
        assertThat(select.firstAncestor(type1, type2) as Any).isInstanceOf(
            ListAstSelect::class.java
        )
        assertThat(select.firstAncestor(type1, type2)).containsOnly(ancestor1, ancestor2)
    }

    @Test
    fun test_descendants() {
        assertThat(select.descendants(mock()) as Any).isSameAs(empty())
        assertThat(select.descendants(mock(), mock()) as Any).isSameAs(empty())
    }

    @Test
    fun test_isEmpty() {
        assertThat(select.isEmpty()).isFalse()
    }

    @Test
    fun test_isNotEmpty() {
        assertThat(select.isNotEmpty()).isTrue()
    }

    @Test
    fun test_filter_by_type() {
        val type = mock<AstNodeType>()
        assertThat(select.filter(type) as Any).isSameAs(empty())
        whenever(node1.type).thenReturn(type)
        assertThat(select.filter(type) as Any).isInstanceOf(SingleAstSelect::class.java)
        assertThat(select.filter(type)).containsOnly(node1)
        whenever(node2.type).thenReturn(type)
        assertThat(select.filter(type) as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.filter(type)).containsOnly(node1, node2)
    }

    @Test
    fun test_filter_by_types() {
        val type1 = mock<AstNodeType>()
        val type2 = mock<AstNodeType>()
        assertThat(select.filter(type1, type2) as Any).isSameAs(empty())
        whenever(node1.`is`(type1, type2)).thenReturn(true)
        assertThat(select.filter(type1, type2) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        assertThat(select.filter(type1, type2)).containsOnly(node1)
        whenever(node2.`is`(type1, type2)).thenReturn(true)
        assertThat(select.filter(type1, type2) as Any).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.filter(type1, type2)).containsOnly(node1, node2)
    }

    @Test
    fun test_filter() {
        val predicate = mock<Predicate<AstNode>>()
        assertThat(select.filter(predicate) as Any?).isSameAs(empty())
        whenever(predicate.test(node1)).thenReturn(true)
        assertThat(select.filter(predicate) as Any?).isInstanceOf(SingleAstSelect::class.java)
        assertThat(select.filter(predicate)).containsOnly(node1)
        whenever(predicate.test(node2)).thenReturn(true)
        assertThat(select.filter(predicate) as Any?).isInstanceOf(ListAstSelect::class.java)
        assertThat(select.filter(predicate)).containsOnly(node1, node2)
    }

    @Test
    fun test_get() {
        assertThat(select[0]).isSameAs(node1)
        assertThat(select[1]).isSameAs(node2)
    }

    @Test
    fun test_get_non_existing() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            select[2]
        }
    }

    @Test
    fun test_size() {
        assertThat(select.size()).isEqualTo(2)
    }

    @Test
    fun test_iterator() {
        assertThat(select.iterator()).containsOnly(node1, node2)
    }
}