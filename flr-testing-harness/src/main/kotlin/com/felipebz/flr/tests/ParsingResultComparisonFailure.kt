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
package com.felipebz.flr.tests

import org.opentest4j.AssertionFailedError

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 *
 * @since 1.16
 */
public class ParsingResultComparisonFailure(override val message: String, expected: String?, actual: String?) :
    AssertionFailedError(
        message, expected, actual
    ) {
    public constructor(expected: String?, actual: String?) : this(
        "$expected\n$actual", expected, actual
    )
}
