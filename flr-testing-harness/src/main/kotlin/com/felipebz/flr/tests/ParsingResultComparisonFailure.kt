//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import org.opentest4j.AssertionFailedError

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class ParsingResultComparisonFailure(override val message: String, expected: String?, actual: String?) :
    AssertionFailedError(
        message, expected, actual
    ) {
    public constructor(expected: String?, actual: String?) : this(
        "$expected\n$actual", expected, actual
    )
}
