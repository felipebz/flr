//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl

/**
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class LexerException : RuntimeException {
    public constructor(message: String?, e: Throwable?) : super(message, e)
    public constructor(message: String?) : super(message)

    public companion object {
        private const val serialVersionUID = 4901910668771476677L
    }
}
