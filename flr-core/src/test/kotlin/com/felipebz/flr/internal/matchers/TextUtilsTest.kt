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
