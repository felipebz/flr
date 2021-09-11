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
package com.sonar.sslr.impl.ast

import com.sonar.sslr.api.*
import java.util.*

public class AstWalker(vararg visitors: AstVisitor) {
    private val visitorsByNodeType: MutableMap<AstNodeType, Array<AstVisitor>> = IdentityHashMap()
    private val visitors = mutableListOf<AstVisitor>()
    private var astAndTokenVisitors = emptyArray<AstAndTokenVisitor>()
    private var lastVisitedToken: Token? = null

    init {
        for (visitor in visitors) {
            addVisitor(visitor)
        }
    }

    public fun addVisitor(visitor: AstVisitor) {
        visitors.add(visitor)
        for (type in visitor.getAstNodeTypesToVisit()) {
            val visitorsByType = getAstVisitors(type)
            visitorsByType.add(visitor)
            putAstVisitors(type, visitorsByType)
        }
        if (visitor is AstAndTokenVisitor) {
            val tokenVisitorsList = astAndTokenVisitors.toMutableList()
            tokenVisitorsList.add(visitor)
            astAndTokenVisitors = tokenVisitorsList.toTypedArray()
        }
    }

    public fun walkAndVisit(ast: AstNode) {
        for (visitor in visitors) {
            visitor.visitFile(ast)
        }
        visit(ast)
        for (i in visitors.indices.reversed()) {
            visitors[i].leaveFile(ast)
        }
    }

    private fun visit(ast: AstNode) {
        val nodeVisitors = getNodeVisitors(ast)
        visitNode(ast, nodeVisitors)
        visitToken(ast)
        visitChildren(ast)
        leaveNode(ast, nodeVisitors)
    }

    private fun visitChildren(ast: AstNode) {
        for (child in ast.children) {
            visit(child)
        }
    }

    private fun visitToken(ast: AstNode) {
        if (ast.hasToken() && lastVisitedToken !== ast.token) {
            lastVisitedToken = ast.token
            for (astAndTokenVisitor in astAndTokenVisitors) {
                astAndTokenVisitor.visitToken(lastVisitedToken)
            }
        }
    }

    private fun getNodeVisitors(ast: AstNode): Array<AstVisitor> {
        var nodeVisitors = visitorsByNodeType[ast.type]
        if (nodeVisitors == null) {
            nodeVisitors = emptyArray()
        }
        return nodeVisitors
    }

    private fun putAstVisitors(type: AstNodeType, visitors: List<AstVisitor>) {
        visitorsByNodeType[type] = visitors.toTypedArray()
    }

    private fun getAstVisitors(type: AstNodeType?): MutableList<AstVisitor> {
        val visitorsByType = visitorsByNodeType[type]
        return visitorsByType?.toMutableList() ?: mutableListOf()
    }

    private companion object {
        private fun leaveNode(ast: AstNode?, nodeVisitors: Array<AstVisitor>) {
            for (i in nodeVisitors.indices.reversed()) {
                nodeVisitors[i].leaveNode(ast)
            }
        }

        private fun visitNode(ast: AstNode?, nodeVisitors: Array<AstVisitor>) {
            for (nodeVisitor in nodeVisitors) {
                nodeVisitor.visitNode(ast)
            }
        }
    }
}