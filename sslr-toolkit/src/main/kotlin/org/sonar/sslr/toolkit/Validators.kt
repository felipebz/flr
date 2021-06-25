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
package org.sonar.sslr.toolkit

import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException

/**
 * Provides a few handy configuration property validators out-of-the-box.
 *
 * @since 1.17
 */
object Validators {
    private val CHARSET_VALIDATOR = CharsetValidator()
    private val BOOLEAN_VALIDATOR = BooleanValidator()

    /**
     * Validates that the property holds a valid [Charset] name.
     *
     * @return A charset validator
     */
    @JvmStatic
    fun charsetValidator(): ValidationCallback {
        return CHARSET_VALIDATOR
    }

    /**
     * Validates that the property holds an integer within the given lower and upper bounds.
     *
     * @param lowerBound
     * @param upperBound
     * @return An integer range validator
     */
    @JvmStatic
    fun integerRangeValidator(lowerBound: Int, upperBound: Int): ValidationCallback {
        return IntegerRangeValidator(lowerBound, upperBound)
    }

    /**
     * Validates that the property holds a boolean value, i.e. either "true" or "false".
     *
     * @return A boolean validator
     */
    @JvmStatic
    fun booleanValidator(): ValidationCallback {
        return BOOLEAN_VALIDATOR
    }

    private class CharsetValidator : ValidationCallback {
        override fun validate(newValueCandidate: String): String {
            return try {
                Charset.forName(newValueCandidate)
                ""
            } catch (e: IllegalCharsetNameException) {
                "Illegal charset: " + e.message
            } catch (e: UnsupportedCharsetException) {
                "Unsupported charset: " + e.message
            }
        }
    }

    private class IntegerRangeValidator(lowerBound: Int, upperBound: Int) : ValidationCallback {
        private val lowerBound: Int
        private val upperBound: Int
        override fun validate(newValueCandidate: String): String {
            return try {
                val value = newValueCandidate.toInt()
                if (value < lowerBound || value > upperBound) {
                    getErrorMessage(value)
                } else {
                    ""
                }
            } catch (e: NumberFormatException) {
                "Not an integer: $newValueCandidate"
            }
        }

        private fun getErrorMessage(value: Int): String {
            return when {
                lowerBound == upperBound -> {
                    "Must be equal to $lowerBound: $value"
                }
                upperBound == Int.MAX_VALUE -> {
                    when (lowerBound) {
                        0 -> {
                            "Must be positive or 0: $value"
                        }
                        1 -> {
                            "Must be strictly positive: $value"
                        }
                        else -> {
                            "Must be greater or equal to $lowerBound: $value"
                        }
                    }
                }
                lowerBound == Int.MIN_VALUE -> {
                    when (upperBound) {
                        0 -> {
                            "Must be negative or 0: $value"
                        }
                        -1 -> {
                            "Must be strictly negative: $value"
                        }
                        else -> {
                            "Must be lower or equal to $upperBound: $value"
                        }
                    }
                }
                else -> {
                    "Must be between $lowerBound and $upperBound: $value"
                }
            }
        }

        init {
            require(lowerBound <= upperBound) { "lowerBound($lowerBound) <= upperBound($upperBound)" }
            this.lowerBound = lowerBound
            this.upperBound = upperBound
        }
    }

    private class BooleanValidator : ValidationCallback {
        override fun validate(newValueCandidate: String): String {
            return if ("false" != newValueCandidate && "true" != newValueCandidate) "Must be either \"true\" or \"false\": $newValueCandidate" else ""
        }
    }
}