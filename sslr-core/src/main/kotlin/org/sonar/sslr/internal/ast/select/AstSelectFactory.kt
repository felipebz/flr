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
package org.sonar.sslr.internal.ast.select

import com.sonar.sslr.api.AstNode
import org.sonar.sslr.ast.AstSelect

object AstSelectFactory {
    private val EMPTY: AstSelect = EmptyAstSelect()

    @JvmStatic
    fun select(node: AstNode?): AstSelect {
        return node?.let { SingleAstSelect(it) } ?: EMPTY
    }

    @JvmStatic
    fun create(list: List<AstNode>): AstSelect {
        return when {
            list.size == 1 -> {
                SingleAstSelect(list[0])
            }
            list.isNotEmpty() -> {
                ListAstSelect(list)
            }
            else -> {
                EMPTY
            }
        }
    }

    @JvmStatic
    fun empty(): AstSelect {
        return EMPTY
    }
}