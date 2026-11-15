//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Constructor

class AssertionsTest {
    @Test
    fun private_constructor() {
        val constructor: Constructor<*> = Assertions::class.java.getDeclaredConstructor()
        assertThat(constructor.isAccessible).isFalse()
        constructor.isAccessible = true
        constructor.newInstance()
    }
}
