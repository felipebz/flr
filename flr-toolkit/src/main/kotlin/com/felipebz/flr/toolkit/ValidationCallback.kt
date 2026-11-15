//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

/**
 * This interface is used to validate values that are to be assigned to configuration properties.
 *
 * The [Validators] class provides out-of-the-box handy validators.
 */
public fun interface ValidationCallback {
    /**
     * Validate the new value candidate.
     *
     * @param newValueCandidate The value to be validated
     * @return The empty string if validation passed, else a non-empty error message
     */
    public fun validate(newValueCandidate: String): String
}
