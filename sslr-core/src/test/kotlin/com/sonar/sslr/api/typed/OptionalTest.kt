/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package com.sonar.sslr.api.typed

import com.sonar.sslr.api.typed.Optional.Companion.absent
import com.sonar.sslr.api.typed.Optional.Companion.of
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class OptionalTest {
    private val present = of<String?>("foo")
    private val absent = absent<String?>()
    @Test
    fun present() {
        assertThat(present.isPresent).isTrue()
        assertThat(present.orNull()).isSameAs("foo")
        assertThat(present.or("bar")).isSameAs("foo")
        assertThat(present.get()).isSameAs("foo")
        assertThat(present.toString()).isEqualTo("Optional.of(foo)")
        assertThat(present == present).isTrue()
        assertThat(present == of("foo")).isTrue()
        assertThat(present == of("bar")).isFalse()
        assertThat(present == absent).isFalse()
        assertThat(present.hashCode()).isEqualTo(0x598df91c + "foo".hashCode())
    }

    @Test
    fun absent() {
        assertThat(absent.isPresent).isFalse()
        assertThat(absent.orNull()).isNull()
        assertThat(absent.or("bar")).isSameAs("bar")
        assertThat(absent.toString()).isEqualTo("Optional.absent()")
        assertThat(absent == present).isFalse()
        assertThat(absent == absent).isTrue()
        assertThat(absent.hashCode()).isEqualTo(0x598df91c)
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