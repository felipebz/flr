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
package com.felipebz.flr.impl.xpath

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AstNodeNavigatorTest {

    private lateinit var navigator: AstNodeNavigator

    @BeforeEach
    fun setUp() {
        navigator = AstNodeNavigator()
    }

    @Test
    fun getTextStringValue() {
        assertThrows<UnsupportedOperationException> {
            navigator.getTextStringValue(null)
        }
    }

    @Test
    fun getCommentStringValue() {
        assertThrows<UnsupportedOperationException> {
            navigator.getCommentStringValue(null)
        }
    }

    @Test
    fun getAttributeStringValue() {
        val astNode = AstNode(
            Token.builder()
                .setType(GenericTokenType.IDENTIFIER)
                .setLine(1)
                .setColumn(2)
                .setValueAndOriginalValue("foo", "bar")
                .build()
        )
        assertThat(navigator.getAttributeStringValue(AstNodeNavigator.Attribute("tokenLine", astNode))).isEqualTo("1")
        assertThat(navigator.getAttributeStringValue(AstNodeNavigator.Attribute("tokenColumn", astNode))).isEqualTo("2")
        assertThat(navigator.getAttributeStringValue(AstNodeNavigator.Attribute("tokenValue", astNode))).isEqualTo("foo")
    }

    @Test
    fun getAttributeStringValue2() {
        val attribute = mock<AstNodeNavigator.Attribute>()
        whenever(attribute.name).thenReturn("foo")
        assertThrows<UnsupportedOperationException> {
            navigator.getAttributeStringValue(attribute)
        }
    }

    @Test
    fun getElementStringValue() {
        assertThrows<UnsupportedOperationException>("Implicit nodes to string conversion is not supported. Use the tokenValue attribute instead.") {
            navigator.getElementStringValue(null)
        }
    }

    /* Namespaces */

    /* Namespaces */
    @Test
    fun getNamespacePrefix() {
        assertThrows<UnsupportedOperationException> {
            navigator.getNamespacePrefix(null)
        }
    }

    @Test
    fun getNamespaceStringValue() {
        assertThrows<UnsupportedOperationException> {
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
        whenever(astNode.parentOrNull).thenReturn(rootAstNode)
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
        assertThrows<UnsupportedOperationException> {
            navigator.getChildAxisIterator(Any())
        }
    }

    @Test
    fun getParentNode() {
        val rootAstNode = mock<AstNode>()
        val astNode = mock<AstNode>()
        whenever(astNode.parentOrNull).thenReturn(rootAstNode)
        val attribute: AstNodeNavigator.Attribute = mock()
        whenever(attribute.astNode).thenReturn(astNode)
        assertThat(navigator.getParentNode(attribute)).isSameAs(astNode)
        assertThat(navigator.getParentNode(astNode)).isSameAs(rootAstNode)
    }

    @Test
    fun getParentNode2() {
        assertThrows<UnsupportedOperationException> {
            navigator.getParentNode(Any())
        }
    }

    @Test
    fun getParentAxisIterator() {
        assertThrows<UnsupportedOperationException> {
            navigator.getParentAxisIterator(Any())
        }
    }

    @Test
    fun getAttributeAxisIterator() {
        assertThrows<UnsupportedOperationException> {
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
