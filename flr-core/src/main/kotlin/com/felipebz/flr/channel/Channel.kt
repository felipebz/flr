//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.channel

public fun interface Channel<O> {
    /**
     * Tries to consume the character stream at the current reading cursor position (provided by the [com.felipebz.flr.channel.CodeReader]). If
     * the character stream is consumed the method must return true and the OUTPUT object can be fed.
     *
     * @param code
     * the handle on the input character stream
     * @param output
     * the OUTPUT that can be optionally fed by the Channel
     * @return false if the Channel doesn't want to consume the character stream, true otherwise.
     */
    public fun consume(code: CodeReader, output: O): Boolean
}
