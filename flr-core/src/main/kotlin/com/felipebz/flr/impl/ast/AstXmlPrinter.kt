//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.ast

import com.felipebz.flr.api.AstNode
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

public class AstXmlPrinter private constructor(private val rootNode: AstNode, private val writer: Writer) {
    private fun print() {
        try {
            printNode(0, rootNode)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun printNode(level: Int, node: AstNode) {
        if (level != 0) {
            writer.append("\n")
        }
        appendSpaces(level)
        if (node.hasChildren()) {
            writer.append("<")
            appendNodecontent(node)
            writer.append(">")
            toXmlChildren(level, node)
            appendCarriageReturnAndSpaces(level)
            writer.append("</").append(node.name).append(">")
        } else {
            writer.append("<")
            appendNodecontent(node)
            writer.append("/>")
        }
    }

    @Throws(IOException::class)
    private fun appendNodecontent(node: AstNode) {
        writer.append(node.name)
        if (node.tokenValue.isNotEmpty()) {
            writer.append(" tokenValue=\"" + node.tokenValue + "\"")
        }
        if (node.hasToken()) {
            writer.append(" tokenLine=\"" + node.tokenLine + "\" tokenColumn=\"" + node.token.column + "\"")
        }
    }

    @Throws(IOException::class)
    private fun toXmlChildren(level: Int, node: AstNode) {
        for (child in node.children) {
            printNode(level + 1, child)
        }
    }

    @Throws(IOException::class)
    private fun appendCarriageReturnAndSpaces(level: Int) {
        writer.append("\n")
        appendSpaces(level)
    }

    @Throws(IOException::class)
    private fun appendSpaces(level: Int) {
        for (i in 0 until level) {
            writer.append("  ")
        }
    }

    public companion object {
        @JvmStatic
        public fun print(rootNode: AstNode): String {
            val writer = StringWriter()
            print(rootNode, writer)
            return writer.toString()
        }

        @JvmStatic
        public fun print(rootNode: AstNode, writer: Writer) {
            val printer = AstXmlPrinter(rootNode, writer)
            printer.print()
        }
    }
}
