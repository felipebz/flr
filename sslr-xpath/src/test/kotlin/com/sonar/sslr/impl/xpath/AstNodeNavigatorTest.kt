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
package com.sonar.sslr.impl.xpath

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.net.URI


class AstNodeNavigatorTest {

    private lateinit var navigator: AstNodeNavigator

    @Before
    fun setUp() {
        navigator = AstNodeNavigator()
    }

    @Test
    fun getTextStringValue() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getTextStringValue(null)
        }
    }

    @Test
    fun getCommentStringValue() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getCommentStringValue(null)
        }
    }

    @Test
    @Throws(Exception::class)
    fun getAttributeStringValue() {
        val astNode = AstNode(Token.builder()
                .setURI(URI("tests://unittest"))
                .setType(GenericTokenType.IDENTIFIER)
                .setLine(1)
                .setColumn(2)
                .setValueAndOriginalValue("foo", "bar")
                .build())
        assertThat(navigator.getAttributeStringValue(AstNodeNavigator.Attribute("tokenLine", astNode))).isEqualTo("1")
        assertThat(navigator.getAttributeStringValue(AstNodeNavigator.Attribute("tokenColumn", astNode))).isEqualTo("2")
        assertThat(navigator.getAttributeStringValue(AstNodeNavigator.Attribute("tokenValue", astNode))).isEqualTo("foo")
    }

    @Test
    fun getAttributeStringValue2() {
        val attribute = mock<AstNodeNavigator.Attribute>()
        whenever(attribute.name).thenReturn("foo")
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getAttributeStringValue(attribute)
        }
    }

    @Test
    fun getElementStringValue() {
        assertThrows("Implicit nodes to string conversion is not supported. Use the tokenValue attribute instead.", UnsupportedOperationException::class.java) {
            navigator.getElementStringValue(null)
        }
    }

    /* Namespaces */

    /* Namespaces */
    @Test
    fun getNamespacePrefix() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getNamespacePrefix(null)
        }
    }

    @Test
    fun getNamespaceStringValue() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getNamespaceStringValue(null)
        }
    }

    /* Attributes */

    /* Attributes */
    @Test
    fun getAttributeName() {
        val attribute = mock<AstNodeNavigator.Attribute>()
        whenever(attribute.name).thenReturn("foo")
        assertThat(navigator.getAttributeName(attribute)).isEqualTo("foo")
    }

    @Test
    fun getAttributeQName() {
        val attribute: AstNodeNavigator.Attribute = mock()
        whenever(attribute.name).thenReturn("foo")
        assertThat(navigator.getAttributeQName(attribute)).isEqualTo("foo")
    }

    /* Elements */

    /* Elements */
    @Test
    fun getAttributeNamespaceUri() {
        assertThat(navigator.getAttributeNamespaceUri(null)).isEqualTo("")
    }

    @Test
    fun getElementName() {
        val astNode = mock<AstNode>()
        whenever(astNode.name).thenReturn("foo")
        assertThat(navigator.getElementName(astNode)).isEqualTo("foo")
    }

    @Test
    fun getElementQName() {
        val astNode: AstNode = mock()
        whenever(astNode.name).thenReturn("foo")
        assertThat(navigator.getElementQName(astNode)).isEqualTo("foo")
    }

    @Test
    fun getElementNamespaceUri() {
        assertThat(navigator.getElementNamespaceUri(null)).isEqualTo("")
    }

    /* Types */

    /* Types */
    @Test
    fun isAttribute() {
        assertThat(navigator.isAttribute(mock<AstNodeNavigator.Attribute>())).isTrue()
        assertThat(navigator.isAttribute(null)).isFalse()
    }

    @Test
    fun isComment() {
        assertThat(navigator.isComment(null)).isFalse()
    }

    @Test
    fun isDocument() {
        val astNode = mock<AstNode>()
        val attribute: AstNodeNavigator.Attribute = mock()
        whenever(attribute.astNode).thenReturn(astNode)
        assertThat(navigator.isDocument(attribute)).isFalse()
        assertThat(navigator.isDocument(astNode)).isFalse()
        assertThat(navigator.isDocument(navigator.getDocumentNode(astNode))).isTrue()
    }

    @Test
    fun isDocument2() {
        assertThat(navigator.isDocument(null)).isFalse()
    }

    @Test
    fun isElement() {
        assertThat(navigator.isElement(mock<AstNode>())).isTrue()
        assertThat(navigator.isElement(null)).isFalse()
    }

    @Test
    fun isNamespace() {
        assertThat(navigator.isNamespace(null)).isFalse()
    }

    @Test
    fun isProcessingInstruction() {
        assertThat(navigator.isProcessingInstruction(null)).isFalse()
    }

    @Test
    fun isText() {
        assertThat(navigator.isText(null)).isFalse()
    }

    /* Navigation */

    /* Navigation */
    @Test
    fun getDocumentNode() {
        val rootAstNode = mock<AstNode>()
        val astNode = mock<AstNode>()
        whenever(astNode.parent).thenReturn(rootAstNode)
        val attribute = mock<AstNodeNavigator.Attribute>()
        whenever(attribute.astNode).thenReturn(astNode)
        val documentNode = navigator.getDocumentNode(attribute) as AstNode
        assertThat(documentNode.name).isEqualTo("[root]")
    }

    @Test
    fun getChildAxisIterator() {
        val attribute = mock<AstNodeNavigator.Attribute>()
        assertThat(navigator.getChildAxisIterator(attribute).hasNext()).isFalse()
    }

    @Test
    fun getChildAxisIterator2() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getChildAxisIterator(Any())
        }
    }

    @Test
    fun getParentNode() {
        val rootAstNode = mock<AstNode>()
        val astNode = mock<AstNode>()
        whenever(astNode.parent).thenReturn(rootAstNode)
        val attribute: AstNodeNavigator.Attribute = mock()
        whenever(attribute.astNode).thenReturn(astNode)
        assertThat(navigator.getParentNode(attribute)).isSameAs(astNode)
        assertThat(navigator.getParentNode(astNode)).isSameAs(rootAstNode)
    }

    @Test
    fun getParentNode2() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getParentNode(Any())
        }
    }

    @Test
    fun getParentAxisIterator() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getParentAxisIterator(Any())
        }
    }

    @Test
    fun getAttributeAxisIterator() {
        assertThrows(UnsupportedOperationException::class.java) {
            navigator.getAttributeAxisIterator(Any())
        }
    }

    /* Unknown */

    /* Unknown */
    @Test
    fun parseXPath() {
        assertThat(navigator.parseXPath(null)).isNull()
    }
}