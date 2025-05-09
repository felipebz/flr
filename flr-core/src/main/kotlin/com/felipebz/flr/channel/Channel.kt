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
