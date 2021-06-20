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
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock
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
        node1 = Mockito.mock(AstNode::class.java)
        node2 = Mockito.mock(AstNode::class.java)
        select = ListAstSelect(listOf(node1, node2))
    }

    @Test
    fun test_children_when_no_children() {
        Assertions.assertThat(select.children() as Any).isSameAs(empty())
        Assertions.assertThat(select.children(Mockito.mock(AstNodeType::class.java)) as Any).isSameAs(empty())
        Assertions.assertThat(
            select.children(
                Mockito.mock(AstNodeType::class.java), Mockito.mock(
                    AstNodeType::class.java
                )
            ) as Any
        ).isSameAs(empty())
    }

    @Test
    fun test_children_when_one_child() {
        val type1 = Mockito.mock(AstNodeType::class.java)
        val type2 = Mockito.mock(AstNodeType::class.java)
        val child = Mockito.mock(AstNode::class.java)
        Mockito.`when`<List<AstNode>>(node1.children).thenReturn(listOf(child))
        Mockito.`when`(node1.firstChild).thenReturn(child)
        var children = select.children()
        Assertions.assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child)
        Mockito.`when`<List<AstNode>>(node1.children).thenReturn(listOf(child))
        children = select.children(type1)
        Assertions.assertThat(children as Any).isSameAs(empty())
        Mockito.`when`(child.type).thenReturn(type1)
        children = select.children(type1)
        Assertions.assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child)
        children = select.children(type1, type2)
        Assertions.assertThat(children as Any).isSameAs(empty())
        Mockito.`when`(child.`is`(type1, type2)).thenReturn(true)
        children = select.children(type1, type2)
        Assertions.assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child)
    }

    @Test
    fun test_chilren_when_more_than_one_child() {
        val type1 = Mockito.mock(AstNodeType::class.java)
        val type2 = Mockito.mock(AstNodeType::class.java)
        val child1 = Mockito.mock(AstNode::class.java)
        val child2 = Mockito.mock(AstNode::class.java)
        Mockito.`when`<List<AstNode>>(node1.children).thenReturn(listOf(child1))
        Mockito.`when`<List<AstNode>>(node2.children).thenReturn(listOf(child2))
        var children = select.children()
        Assertions.assertThat(children as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child1, child2)
        children = select.children(type1)
        Assertions.assertThat(children as Any).isSameAs(empty())
        Mockito.`when`(child1.type).thenReturn(type1)
        children = select.children(type1)
        Assertions.assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child1)
        Mockito.`when`(child2.type).thenReturn(type1)
        children = select.children(type1)
        Assertions.assertThat(children as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child1, child2)
        children = select.children(type1, type2)
        Assertions.assertThat(children as Any).isSameAs(empty())
        Mockito.`when`(child1.`is`(type1, type2)).thenReturn(true)
        children = select.children(type1, type2)
        Assertions.assertThat(children as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child1)
        Mockito.`when`(child2.`is`(type1, type2)).thenReturn(true)
        children = select.children(type1, type2)
        Assertions.assertThat(children as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(children).containsOnly(child1, child2)
    }

    @Test
    fun test_nextSibling() {
        Assertions.assertThat(select.nextSibling() as Any).isSameAs(empty())
        val sibling1 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node1.nextSibling).thenReturn(sibling1)
        Assertions.assertThat(select.nextSibling() as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(select.nextSibling()).containsOnly(sibling1)
        val sibling2 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node2.nextSibling).thenReturn(sibling2)
        Assertions.assertThat(select.nextSibling() as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.nextSibling()).containsOnly(sibling1, sibling2)
    }

    @Test
    fun test_previousSibling() {
        Assertions.assertThat(select.previousSibling() as Any).isSameAs(empty())
        val sibling1 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node1.previousSibling).thenReturn(sibling1)
        Assertions.assertThat(select.previousSibling() as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(select.previousSibling()).containsOnly(sibling1)
        val sibling2 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node2.previousSibling).thenReturn(sibling2)
        Assertions.assertThat(select.previousSibling() as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.previousSibling()).containsOnly(sibling1, sibling2)
    }

    @Test
    fun test_parent() {
        Assertions.assertThat(select.parent() as Any).isSameAs(empty())
        val parent1 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node1.parent).thenReturn(parent1)
        Assertions.assertThat(select.parent() as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(select.parent()).containsOnly(parent1)
        val parent2 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node2.parent).thenReturn(parent2)
        Assertions.assertThat(select.parent() as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.parent()).containsOnly(parent1, parent2)
    }

    @Test
    fun test_firstAncestor_by_type() {
        val type = Mockito.mock(AstNodeType::class.java)
        Assertions.assertThat(select.firstAncestor(type) as Any).isSameAs(empty())
        val parent = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node1.parent).thenReturn(parent)
        val ancestor1 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(ancestor1.type).thenReturn(type)
        Mockito.`when`(parent.parent).thenReturn(ancestor1)
        Assertions.assertThat(select.firstAncestor(type) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        Assertions.assertThat(select.firstAncestor(type)).containsOnly(ancestor1)
        val ancestor2 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(ancestor2.type).thenReturn(type)
        Mockito.`when`(node2.parent).thenReturn(ancestor2)
        Assertions.assertThat(select.firstAncestor(type) as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.firstAncestor(type)).containsOnly(ancestor1, ancestor2)
    }

    @Test
    fun test_firstAncestor_by_types() {
        val type1 = Mockito.mock(AstNodeType::class.java)
        val type2 = Mockito.mock(AstNodeType::class.java)
        Assertions.assertThat(select.firstAncestor(type1, type2) as Any).isSameAs(empty())
        val parent = Mockito.mock(AstNode::class.java)
        Mockito.`when`(node1.parent).thenReturn(parent)
        val ancestor1 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(ancestor1.`is`(type1, type2)).thenReturn(true)
        Mockito.`when`(parent.parent).thenReturn(ancestor1)
        Assertions.assertThat(select.firstAncestor(type1, type2) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        Assertions.assertThat(select.firstAncestor(type1, type2)).containsOnly(ancestor1)
        val ancestor2 = Mockito.mock(AstNode::class.java)
        Mockito.`when`(ancestor2.`is`(type1, type2)).thenReturn(true)
        Mockito.`when`(node2.parent).thenReturn(ancestor2)
        Assertions.assertThat(select.firstAncestor(type1, type2) as Any).isInstanceOf(
            ListAstSelect::class.java
        )
        Assertions.assertThat(select.firstAncestor(type1, type2)).containsOnly(ancestor1, ancestor2)
    }

    @Test
    fun test_descendants() {
        Assertions.assertThat(select.descendants(Mockito.mock(AstNodeType::class.java)) as Any).isSameAs(empty())
        Assertions.assertThat(
            select.descendants(
                Mockito.mock(AstNodeType::class.java), Mockito.mock(
                    AstNodeType::class.java
                )
            ) as Any
        ).isSameAs(empty())
    }

    @Test
    fun test_isEmpty() {
        Assertions.assertThat(select.isEmpty()).isFalse()
    }

    @Test
    fun test_isNotEmpty() {
        Assertions.assertThat(select.isNotEmpty()).isTrue()
    }

    @Test
    fun test_filter_by_type() {
        val type = Mockito.mock(AstNodeType::class.java)
        Assertions.assertThat(select.filter(type) as Any).isSameAs(empty())
        Mockito.`when`(node1.type).thenReturn(type)
        Assertions.assertThat(select.filter(type) as Any).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(select.filter(type)).containsOnly(node1)
        Mockito.`when`(node2.type).thenReturn(type)
        Assertions.assertThat(select.filter(type) as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.filter(type)).containsOnly(node1, node2)
    }

    @Test
    fun test_filter_by_types() {
        val type1 = Mockito.mock(AstNodeType::class.java)
        val type2 = Mockito.mock(AstNodeType::class.java)
        Assertions.assertThat(select.filter(type1, type2) as Any).isSameAs(empty())
        Mockito.`when`(node1.`is`(type1, type2)).thenReturn(true)
        Assertions.assertThat(select.filter(type1, type2) as Any).isInstanceOf(
            SingleAstSelect::class.java
        )
        Assertions.assertThat(select.filter(type1, type2)).containsOnly(node1)
        Mockito.`when`(node2.`is`(type1, type2)).thenReturn(true)
        Assertions.assertThat(select.filter(type1, type2) as Any).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.filter(type1, type2)).containsOnly(node1, node2)
    }

    @Test
    fun test_filter() {
        val predicate = mock<Predicate<AstNode>>()
        Assertions.assertThat(select.filter(predicate) as Any?).isSameAs(empty())
        Mockito.`when`(predicate.test(node1)).thenReturn(true)
        Assertions.assertThat(select.filter(predicate) as Any?).isInstanceOf(SingleAstSelect::class.java)
        Assertions.assertThat(select.filter(predicate)).containsOnly(node1)
        Mockito.`when`(predicate.test(node2)).thenReturn(true)
        Assertions.assertThat(select.filter(predicate) as Any?).isInstanceOf(ListAstSelect::class.java)
        Assertions.assertThat(select.filter(predicate)).containsOnly(node1, node2)
    }

    @Test
    fun test_get() {
        Assertions.assertThat(select[0]).isSameAs(node1)
        Assertions.assertThat(select[1]).isSameAs(node2)
    }

    @Test
    fun test_get_non_existing() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            select[2]
        }
    }

    @Test
    fun test_size() {
        Assertions.assertThat(select.size()).isEqualTo(2)
    }

    @Test
    fun test_iterator() {
        Assertions.assertThat(select.iterator()).containsOnly(node1, node2)
    }
}