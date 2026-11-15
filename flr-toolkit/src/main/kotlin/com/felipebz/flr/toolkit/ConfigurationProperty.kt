//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

/**
 * This class represents a configuration property, which is made of a name, a description (which may be empty),
 * a default value, and optionnally a validation callback.
 *
 * @param name
 * @param description
 * @param defaultValue
 * @param validationCallback The validation callback. Note that handy ones are available out-of-the-box by the [Validators] class.
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
        private val NO_VALIDATION: ValidationCallback = ValidationCallback { "" }
    }
}
