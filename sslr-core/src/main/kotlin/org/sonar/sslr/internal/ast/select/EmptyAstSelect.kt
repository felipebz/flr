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
package org.sonar.sslr.internal.ast.select

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.AstNodeType
import org.sonar.sslr.ast.AstSelect
import java.util.*
import java.util.function.Predicate

/**
 * [AstSelect] which contains no elements.
 */
class EmptyAstSelect : AstSelect {
    override fun children(): AstSelect {
        return this
    }

    override fun children(type: AstNodeType): AstSelect {
        return this
    }

    override fun children(vararg types: AstNodeType): AstSelect {
        return this
    }

    override fun nextSibling(): AstSelect {
        return this
    }

    override fun previousSibling(): AstSelect {
        return this
    }

    override fun parent(): AstSelect {
        return this
    }

    override fun firstAncestor(type: AstNodeType): AstSelect {
        return this
    }

    override fun firstAncestor(vararg types: AstNodeType): AstSelect {
        return this
    }

    override fun descendants(type: AstNodeType): AstSelect {
        return this
    }

    override fun descendants(vararg types: AstNodeType): AstSelect {
        return this
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun isNotEmpty(): Boolean {
        return false
    }

    override fun filter(type: AstNodeType): AstSelect {
        return this
    }

    override fun filter(vararg types: AstNodeType): AstSelect {
        return this
    }

    override fun filter(predicate: Predicate<AstNode>): AstSelect {
        return this
    }

    override fun size(): Int {
        return 0
    }

    override fun get(index: Int): AstNode {
        throw IndexOutOfBoundsException()
    }

    override fun iterator(): MutableIterator<AstNode> {
        return Collections.emptyIterator()
    }
}