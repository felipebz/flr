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
package com.sonar.sslr.impl.xpath

import org.jaxen.DefaultNavigator
import com.sonar.sslr.api.AstNode
import org.jaxen.XPath
import java.lang.UnsupportedOperationException
import org.jaxen.util.SingleObjectIterator
import java.util.*

internal open class AstNodeNavigator : DefaultNavigator() {
    @Transient
    private var documentNode: AstNode? = null
    fun reset() {
        documentNode = null
    }

    /* Type conversions */
    override fun getTextStringValue(arg0: Any?): String {
        throw UnsupportedOperationException()
    }

    override fun getCommentStringValue(arg0: Any?): String {
        throw UnsupportedOperationException()
    }

    override fun getAttributeStringValue(attributeObject: Any): String? {
        val attribute = attributeObject as Attribute
        return when (attribute.name) {
            "tokenLine" -> {
                attribute.astNode.token.line.toString()
            }
            "tokenColumn" -> {
                attribute.astNode.token.column.toString()
            }
            "tokenValue" -> {
                attribute.astNode.token.value
            }
            else -> {
                throw UnsupportedOperationException("Unsupported attribute name \"" + attribute.name + "\"")
            }
        }
    }

    override fun getElementStringValue(arg0: Any?): String {
        throw UnsupportedOperationException("Implicit nodes to string conversion is not supported. Use the tokenValue attribute instead.")
    }

    /* Namespaces */
    override fun getNamespacePrefix(arg0: Any?): String {
        throw UnsupportedOperationException()
    }

    override fun getNamespaceStringValue(arg0: Any?): String {
        throw UnsupportedOperationException()
    }

    /* Attributes */
    override fun getAttributeName(attributeObject: Any?): String {
        val attribute = attributeObject as Attribute
        return attribute.name
    }

    override fun getAttributeQName(attributeObject: Any?): String {
        return getAttributeName(attributeObject)
    }

    override fun getAttributeNamespaceUri(arg0: Any?): String {
        return ""
    }

    /* Elements */
    override fun getElementName(astNodeObject: Any): String? {
        val astNode = astNodeObject as AstNode
        return astNode.name
    }

    override fun getElementQName(astNodeObject: Any): String? {
        return getElementName(astNodeObject)
    }

    override fun getElementNamespaceUri(astNodeObject: Any?): String {
        return ""
    }

    /* Types */
    override fun isAttribute(`object`: Any?): Boolean {
        return `object` is Attribute
    }

    override fun isComment(`object`: Any?): Boolean {
        return false
    }

    override fun isDocument(contextObject: Any?): Boolean {
        computeDocumentNode(contextObject)
        return documentNode != null && documentNode == contextObject
    }

    override fun isElement(`object`: Any?): Boolean {
        return `object` is AstNode
    }

    override fun isNamespace(arg0: Any?): Boolean {
        return false
    }

    override fun isProcessingInstruction(arg0: Any?): Boolean {
        return false
    }

    override fun isText(arg0: Any?): Boolean {
        return false
    }

    /* Navigation */
    private fun computeDocumentNode(contextNode: Any?) {
        if (documentNode == null) {
            if (isElement(contextNode)) {
                var root: AstNode? = contextNode as AstNode?
                while (root?.parentOrNull != null) {
                    root = root.parentOrNull
                }
                documentNode = AstNode(null, "[root]", null).apply {
                    addChild(root)
                }
            } else if (isAttribute(contextNode)) {
                val attribute = contextNode as Attribute
                computeDocumentNode(attribute.astNode)
            }
        }
    }

    override fun getDocumentNode(contextNode: Any): Any {
        computeDocumentNode(contextNode)
        return checkNotNull(documentNode) {
            "Unable to compute the document node from the context node \"" + contextNode.javaClass.simpleName + "\": " + contextNode
        }
    }

    override fun getChildAxisIterator(contextNode: Any): Iterator<*> {
        return when {
            isElement(contextNode) -> {
                val astNode = contextNode as AstNode
                astNode.children.iterator()
            }
            isAttribute(contextNode) -> {
                Collections.emptyIterator<Any>()
            }
            else -> {
                throw UnsupportedOperationException("Unsupported context object type for child axis \"" + contextNode.javaClass.simpleName + "\": " + contextNode)
            }
        }
    }

    override fun getParentNode(contextNode: Any): Any? {
        return when {
            isElement(contextNode) -> {
                val astNode = contextNode as AstNode
                astNode.parentOrNull
            }
            isAttribute(contextNode) -> {
                val attribute = contextNode as Attribute
                attribute.astNode
            }
            else -> {
                throw UnsupportedOperationException("Unsupported context object type for parent node \"" + contextNode.javaClass.simpleName + "\": " + contextNode)
            }
        }
    }

    override fun getParentAxisIterator(contextNode: Any): Iterator<*> {
        return when {
            isElement(contextNode) -> {
                val astNode = contextNode as AstNode
                val parent = astNode.parentOrNull
                parent?.let { SingleObjectIterator(it) } ?: Collections.emptyIterator<Any>()
            }
            isAttribute(contextNode) -> {
                val attribute = contextNode as Attribute
                SingleObjectIterator(attribute.astNode)
            }
            else -> {
                throw UnsupportedOperationException("Unsupported context object type for parent axis \"" + contextNode.javaClass.simpleName + "\": " + contextNode)
            }
        }
    }

    override fun getAttributeAxisIterator(contextNode: Any): Iterator<*> {
        return if (isElement(contextNode)) {
            val astNode = contextNode as AstNode
            if (!astNode.hasToken()) {
                Collections.emptyIterator<Any>()
            } else {
                listOf(
                    Attribute("tokenLine", astNode),
                    Attribute("tokenColumn", astNode),
                    Attribute("tokenValue", astNode)
                ).iterator()
            }
        } else if (isAttribute(contextNode)) {
            Collections.emptyIterator<Any>()
        } else {
            throw UnsupportedOperationException("Unsupported context object type for attribute axis \"" + contextNode.javaClass.simpleName + "\": " + contextNode)
        }
    }

    override fun parseXPath(arg0: String?): XPath? {
        return null
    }

    open class Attribute(open val name: String, open val astNode: AstNode)
}