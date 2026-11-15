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

class AlwaysSkipFromAstTest {
    @Test
    fun test() {
        val astNode = mock<AstNode>()
        assertThat(AlwaysSkipFromAst.hasToBeSkippedFromAst(astNode)).isTrue()
    }
}
