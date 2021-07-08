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
package org.sonar.sslr.internal.matchers

import java.io.File
import java.net.URI
import java.util.*

public class TextLocation(private val file: File?, private val uri: URI?, private val line: Int, private val column: Int) {
    public fun getFile(): File? {
        return file
    }

    /**
     * For internal use only.
     */
    public fun getFileURI(): URI? {
        return uri
    }

    public fun getLine(): Int {
        return line
    }

    public fun getColumn(): Int {
        return column
    }

    override fun hashCode(): Int {
        return Objects.hash(file, line, column)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is TextLocation) {
            return (file == other.file
                    && line == other.line && column == other.column)
        }
        return false
    }

    override fun toString(): String {
        return "TextLocation{file=$file, line=$line, column=$column}"
    }
}