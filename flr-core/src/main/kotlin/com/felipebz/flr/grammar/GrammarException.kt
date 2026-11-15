//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.grammar

/**
 * Indicates problem with definition of grammar.
 * If you face with this exception, then you should fix code, which defines your grammar.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public class GrammarException : RuntimeException {
    public constructor(message: String?) : super(message)
    public constructor(cause: Throwable?, message: String?) : super(message, cause)
}
