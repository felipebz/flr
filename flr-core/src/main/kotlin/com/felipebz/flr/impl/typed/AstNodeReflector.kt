//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.typed

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.Token
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
