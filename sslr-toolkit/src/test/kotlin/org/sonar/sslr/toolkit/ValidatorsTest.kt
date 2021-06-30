/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.sslr.toolkit

import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.sonar.sslr.toolkit.Validators.integerRangeValidator

class ValidatorsTest {
    @Test
    fun charsetValidator() {
        val validator = Validators.charsetValidator()
        Assertions.assertThat(validator.validate("UTF-8")).isEmpty()
        Assertions.assertThat(validator.validate("ISO-8859-15")).isEmpty()
        Assertions.assertThat(validator.validate("foo")).isEqualTo("Unsupported charset: foo")
        Assertions.assertThat(validator.validate(" ")).isEqualTo("Illegal charset:  ")
    }

    @Test
    fun charsetValidator_single_instance() {
        Assertions.assertThat(Validators.charsetValidator()).isSameAs(Validators.charsetValidator())
    }

    @Test
    fun integerRangeValidator() {
        val validator = integerRangeValidator(0, 42)
        Assertions.assertThat(validator.validate("24")).isEmpty()
        Assertions.assertThat(validator.validate("-100")).isEqualTo("Must be between 0 and 42: -100")
        Assertions.assertThat(validator.validate("100")).isEqualTo("Must be between 0 and 42: 100")
        Assertions.assertThat(validator.validate("foo")).isEqualTo("Not an integer: foo")
        Assertions.assertThat(integerRangeValidator(42, 42).validate("43")).isEqualTo("Must be equal to 42: 43")
        Assertions.assertThat(integerRangeValidator(Int.MIN_VALUE, 0).validate("1"))
            .isEqualTo("Must be negative or 0: 1")
        Assertions.assertThat(integerRangeValidator(Int.MIN_VALUE, -1).validate("0"))
            .isEqualTo("Must be strictly negative: 0")
        Assertions.assertThat(integerRangeValidator(Int.MIN_VALUE, 42).validate("43"))
            .isEqualTo("Must be lower or equal to 42: 43")
        Assertions.assertThat(integerRangeValidator(0, Int.MAX_VALUE).validate("-1"))
            .isEqualTo("Must be positive or 0: -1")
        Assertions.assertThat(integerRangeValidator(1, Int.MAX_VALUE).validate("0"))
            .isEqualTo("Must be strictly positive: 0")
        Assertions.assertThat(integerRangeValidator(42, Int.MAX_VALUE).validate("41"))
            .isEqualTo("Must be greater or equal to 42: 41")
    }

    @Test
    fun integerRangeValidator_should_fail_with_upper_smaller_than_lower_bound() {
        assertThrows("lowerBound(42) <= upperBound(0)", IllegalArgumentException::class.java) {
            integerRangeValidator(42, 0)
        }
    }

    @Test
    fun booleanValidator() {
        val validator = Validators.booleanValidator()
        Assertions.assertThat(validator.validate("true")).isEmpty()
        Assertions.assertThat(validator.validate("false")).isEmpty()
        Assertions.assertThat(validator.validate("foo")).isEqualTo("Must be either \"true\" or \"false\": foo")
    }

    @Test
    fun booleanValidator_single_instance() {
        Assertions.assertThat(Validators.booleanValidator()).isSameAs(Validators.booleanValidator())
    }
}