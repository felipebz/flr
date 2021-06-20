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
package com.sonar.sslr.api.typed

import com.sonar.sslr.api.typed.Optional.Companion.absent
import com.sonar.sslr.api.typed.Optional.Companion.of
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test

class OptionalTest {
    private val present = of<String?>("foo")
    private val absent = absent<String?>()
    @Test
    fun present() {
        Assertions.assertThat(present.isPresent).isTrue()
        Assertions.assertThat(present.orNull()).isSameAs("foo")
        Assertions.assertThat(present.or("bar")).isSameAs("foo")
        Assertions.assertThat(present.get()).isSameAs("foo")
        Assertions.assertThat(present.toString()).isEqualTo("Optional.of(foo)")
        Assertions.assertThat(present == present).isTrue()
        Assertions.assertThat(present == of("foo")).isTrue()
        Assertions.assertThat(present == of("bar")).isFalse()
        Assertions.assertThat(present == absent).isFalse()
        Assertions.assertThat(present.hashCode()).isEqualTo(0x598df91c + "foo".hashCode())
    }

    @Test
    fun absent() {
        Assertions.assertThat(absent.isPresent).isFalse()
        Assertions.assertThat(absent.orNull()).isNull()
        Assertions.assertThat(absent.or("bar")).isSameAs("bar")
        Assertions.assertThat(absent.toString()).isEqualTo("Optional.absent()")
        Assertions.assertThat(absent == present).isFalse()
        Assertions.assertThat(absent == absent).isTrue()
        Assertions.assertThat(absent.hashCode()).isEqualTo(0x598df91c)
        assertThrows("value is absent", IllegalStateException::class.java) {
            absent.get()
        }
    }

    @Test
    fun present_or_null() {
        assertThrows("use orNull() instead of or(null)", IllegalArgumentException::class.java) {
            present.or(null)
        }
    }

    @Test
    fun absent_or_null() {
        assertThrows("use orNull() instead of or(null)", IllegalArgumentException::class.java) {
            absent.or(null)
        }
    }
}