//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ErrorLocatingHandlerTest {
    private val errorLocatingHandler = ErrorLocatingHandler()
    @Test
    fun should_find_location_of_error() {
        val machine = mock<Machine>()
        whenever(machine.index).thenReturn(1)
        errorLocatingHandler.onBacktrack(machine)
        assertThat(errorLocatingHandler.getErrorIndex()).isEqualTo(1)
        whenever(machine.index).thenReturn(3)
        errorLocatingHandler.onBacktrack(machine)
        assertThat(errorLocatingHandler.getErrorIndex()).isEqualTo(3)
        whenever(machine.index).thenReturn(2)
        errorLocatingHandler.onBacktrack(machine)
        assertThat(errorLocatingHandler.getErrorIndex()).isEqualTo(3)
    }
}
