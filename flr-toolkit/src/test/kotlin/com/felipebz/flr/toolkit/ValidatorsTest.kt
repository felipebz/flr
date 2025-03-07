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
package com.felipebz.flr.toolkit

import com.felipebz.flr.toolkit.Validators.integerRangeValidator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidatorsTest {
    @Test
    fun charsetValidator() {
        val validator = Validators.charsetValidator()
        assertThat(validator.validate("UTF-8")).isEmpty()
        assertThat(validator.validate("ISO-8859-15")).isEmpty()
        assertThat(validator.validate("foo")).isEqualTo("Unsupported charset: foo")
        assertThat(validator.validate(" ")).isEqualTo("Illegal charset:  ")
    }

    @Test
    fun charsetValidator_single_instance() {
        assertThat(Validators.charsetValidator()).isSameAs(Validators.charsetValidator())
    }

    @Test
    fun integerRangeValidator() {
        val validator = integerRangeValidator(0, 42)
        assertThat(validator.validate("24")).isEmpty()
        assertThat(validator.validate("-100")).isEqualTo("Must be between 0 and 42: -100")
        assertThat(validator.validate("100")).isEqualTo("Must be between 0 and 42: 100")
        assertThat(validator.validate("foo")).isEqualTo("Not an integer: foo")
        assertThat(integerRangeValidator(42, 42).validate("43")).isEqualTo("Must be equal to 42: 43")
        assertThat(integerRangeValidator(Int.MIN_VALUE, 0).validate("1"))
            .isEqualTo("Must be negative or 0: 1")
        assertThat(integerRangeValidator(Int.MIN_VALUE, -1).validate("0"))
            .isEqualTo("Must be strictly negative: 0")
        assertThat(integerRangeValidator(Int.MIN_VALUE, 42).validate("43"))
            .isEqualTo("Must be lower or equal to 42: 43")
        assertThat(integerRangeValidator(0, Int.MAX_VALUE).validate("-1"))
            .isEqualTo("Must be positive or 0: -1")
        assertThat(integerRangeValidator(1, Int.MAX_VALUE).validate("0"))
            .isEqualTo("Must be strictly positive: 0")
        assertThat(integerRangeValidator(42, Int.MAX_VALUE).validate("41"))
            .isEqualTo("Must be greater or equal to 42: 41")
    }

    @Test
    fun integerRangeValidator_should_fail_with_upper_smaller_than_lower_bound() {
        assertThrows<IllegalArgumentException>("lowerBound(42) <= upperBound(0)") {
            integerRangeValidator(42, 0)
        }
    }

    @Test
    fun booleanValidator() {
        val validator = Validators.booleanValidator()
        assertThat(validator.validate("true")).isEmpty()
        assertThat(validator.validate("false")).isEmpty()
        assertThat(validator.validate("foo")).isEqualTo("Must be either \"true\" or \"false\": foo")
    }

    @Test
    fun booleanValidator_single_instance() {
        assertThat(Validators.booleanValidator()).isSameAs(Validators.booleanValidator())
    }
}
