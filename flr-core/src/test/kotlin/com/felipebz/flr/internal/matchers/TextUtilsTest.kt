//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.matchers

import com.felipebz.flr.internal.matchers.TextUtils.escape
import com.felipebz.flr.internal.matchers.TextUtils.trimTrailingLineSeparatorFrom
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.Constructor

class TextUtilsTest {
    @Test
    fun should_escape() {
        assertThat(escape('\r')).isEqualTo("\\r")
        assertThat(escape('\n')).isEqualTo("\\n")
        assertThat(escape('\u000C')).isEqualTo("\\f")
        assertThat(escape('\t')).isEqualTo("\\t")
        assertThat(escape('"')).isEqualTo("\\\"")
        assertThat(escape('\\')).isEqualTo("\\")
    }

    @Test
    fun should_trim_trailing_line_separator() {
        assertThat(trimTrailingLineSeparatorFrom("\r\n")).isEqualTo("")
        assertThat(trimTrailingLineSeparatorFrom("\r\nfoo\r\n")).isEqualTo("\r\nfoo")
    }

    @Test
    fun private_constructor() {
        val constructor: Constructor<*> = TextUtils::class.java.getDeclaredConstructor()
        assertThat(constructor.isAccessible).isFalse()
        constructor.isAccessible = true
        constructor.newInstance()
    }
}
