/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.toolkit

import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConfigurationPropertyTest {

    @Test
    fun name() {
        assertThat(ConfigurationProperty("foo", "", "").name).isEqualTo("foo")
        assertThat(ConfigurationProperty("bar", "", "").name).isEqualTo("bar")
    }

    @Test
    fun description() {
        assertThat(ConfigurationProperty("", "foo", "").description).isEqualTo("foo")
        assertThat(ConfigurationProperty("", "bar", "").description).isEqualTo("bar")
    }

    @Test
    fun validate() {
        assertThat(ConfigurationProperty("", "", "").validate("")).isEmpty()
        assertThat(ConfigurationProperty("", "", "").validate("foo")).isEmpty()
        val property = ConfigurationProperty("", "", "foo") {
            if ("foo" == it) "" else "Only the value \"foo\" is allowed."
        }
        assertThat(property.validate("")).isEqualTo("Only the value \"foo\" is allowed.")
        assertThat(property.validate("foo")).isEmpty()
        assertThat(property.validate("bar")).isEqualTo("Only the value \"foo\" is allowed.")
    }

    @Test
    fun setValue_should_succeed_if_validation_passes() {
        ConfigurationProperty("", "", "").value = ""
        ConfigurationProperty("", "", "").value = "foo"
    }

    @Test
    fun setValue_should_fail_if_validation_fails() {
        assertThrows<IllegalArgumentException>("The value \"foo\" did not pass validation: Not valid!") {
            ConfigurationProperty("", "", "") {
                if (it.isEmpty()) "" else "The value \"$it\" did not pass validation: Not valid!"
            }.value = "foo"
        }
    }

    @Test
    fun value() {
        assertThat(ConfigurationProperty("", "", "").value).isEqualTo("")
        assertThat(ConfigurationProperty("", "", "foo").value).isEqualTo("foo")
        val property = ConfigurationProperty("", "", "")
        assertThat(property.value).isEqualTo("")
        property.value = "foo"
        assertThat(property.value).isEqualTo("foo")
    }
}