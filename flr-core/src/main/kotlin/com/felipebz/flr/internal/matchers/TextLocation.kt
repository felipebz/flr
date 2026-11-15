//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.matchers

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
