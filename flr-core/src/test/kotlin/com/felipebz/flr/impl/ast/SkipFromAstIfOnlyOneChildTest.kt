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
package com.felipebz.flr.impl.ast

import com.felipebz.flr.api.AstNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SkipFromAstIfOnlyOneChildTest {
    @Test
    fun testHasToBeSkippedFromAst() {
        val astNode = mock<AstNode>()
        whenever(astNode.numberOfChildren).thenReturn(1)
        assertThat(SkipFromAstIfOnlyOneChild.hasToBeSkippedFromAst(astNode)).isTrue()
        whenever(astNode.numberOfChildren).thenReturn(2)
        assertThat(SkipFromAstIfOnlyOneChild.hasToBeSkippedFromAst(astNode)).isFalse()
    }
}
