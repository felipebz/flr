//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError

class ParsingResultComparisonFailureTest {
    @Test
    fun test_implicit_message() {
        val failure = ParsingResultComparisonFailure("expected", "actual")
        assertThat(failure)
            .isInstanceOf(AssertionFailedError::class.java)
            .hasMessage("expected\nactual")
            .hasNoCause()
    }

    @Test
    fun test_explicit_message() {
        val failure = ParsingResultComparisonFailure("foo", "expected", "actual")
        assertThat(failure)
            .isInstanceOf(AssertionFailedError::class.java)
            .hasMessage("foo")
            .hasNoCause()
    }
}
