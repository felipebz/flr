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
package com.sonar.sslr.impl.typed

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.Token
import java.lang.reflect.Field

internal object AstNodeReflector {
    private val TOKEN_FIELD = getAstNodeField("token")
    private val CHILD_INDEX_FIELD = getAstNodeField("childIndex")
    private val PARENT_FIELD = getAstNodeField("parent")

    @JvmStatic
    fun setToken(astNode: AstNode?, token: Token?) {
        ReflectionUtils.setField(TOKEN_FIELD, astNode, token)
    }

    @JvmStatic
    fun setChildIndex(astNode: AstNode?, childIndex: Int) {
        ReflectionUtils.setField(CHILD_INDEX_FIELD, astNode, childIndex)
    }

    @JvmStatic
    fun setParent(astNode: AstNode?, parent: AstNode?) {
        ReflectionUtils.setField(PARENT_FIELD, astNode, parent)
    }

    private fun getAstNodeField(name: String): Field {
        return ReflectionUtils.getField(AstNode::class.java, name)
    }
}