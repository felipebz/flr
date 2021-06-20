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
package org.sonar.sslr.internal.matchers

import org.fest.assertions.Assertions
import org.junit.Test
import org.sonar.sslr.internal.matchers.TextUtils.escape
import org.sonar.sslr.internal.matchers.TextUtils.trimTrailingLineSeparatorFrom
import java.lang.reflect.Constructor

class TextUtilsTest {
    @Test
    fun should_escape() {
        Assertions.assertThat(escape('\r')).isEqualTo("\\r")
        Assertions.assertThat(escape('\n')).isEqualTo("\\n")
        Assertions.assertThat(escape('\u000C')).isEqualTo("\\f")
        Assertions.assertThat(escape('\t')).isEqualTo("\\t")
        Assertions.assertThat(escape('"')).isEqualTo("\\\"")
        Assertions.assertThat(escape('\\')).isEqualTo("\\")
    }

    @Test
    fun should_trim_trailing_line_separator() {
        Assertions.assertThat(trimTrailingLineSeparatorFrom("\r\n")).isEqualTo("")
        Assertions.assertThat(trimTrailingLineSeparatorFrom("\r\nfoo\r\n")).isEqualTo("\r\nfoo")
    }

    @Test
    @Throws(Exception::class)
    fun private_constructor() {
        val constructor: Constructor<*> = TextUtils::class.java.getDeclaredConstructor()
        Assertions.assertThat(constructor.isAccessible).isFalse()
        constructor.isAccessible = true
        constructor.newInstance()
    }
}