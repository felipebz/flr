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
package org.sonar.sslr.toolkit

import java.util.*

/**
 * This class represents a configuration property, which is made of a name, a description (which may be empty),
 * a default value, and optionnally a validation callback.
 *
 * @param name
 * @param description
 * @param defaultValue
 * @param validationCallback The validation callback. Note that handy ones are available out-of-the-box by the [Validators] class.
 * @since 1.17
 */
public class ConfigurationProperty @JvmOverloads constructor(
    name: String,
    description: String,
    defaultValue: String,
    validationCallback: ValidationCallback = NO_VALIDATION
) {
    public val name: String
    public val description: String

    public var value: String = ""
        set(value) {
            val errorMessage = validate(value)
            require("" == errorMessage) { "The value \"$value\" did not pass validation: $errorMessage" }
            field = value
        }

    private val validationCallback: ValidationCallback

    init {
        Objects.requireNonNull(name)
        Objects.requireNonNull(description)
        Objects.requireNonNull(defaultValue)
        Objects.requireNonNull(validationCallback)
        val errorMessage = validationCallback.validate(defaultValue)
        require("" == errorMessage) { "The default value \"$defaultValue\" did not pass validation: $errorMessage" }
        this.name = name
        this.description = description
        this.validationCallback = validationCallback
        value = defaultValue
    }

    public fun validate(newValueCandidate: String): String {
        return validationCallback.validate(newValueCandidate)
    }

    public companion object {
        private val NO_VALIDATION: ValidationCallback = object : ValidationCallback {
            override fun validate(newValueCandidate: String): String {
                return ""
            }
        }
    }
}