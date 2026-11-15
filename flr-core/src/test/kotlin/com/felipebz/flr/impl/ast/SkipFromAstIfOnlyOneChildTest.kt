//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
