//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

import org.assertj.core.api.Assertions.assertThat
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
