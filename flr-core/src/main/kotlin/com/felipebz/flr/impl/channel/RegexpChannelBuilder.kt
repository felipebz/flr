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
package com.felipebz.flr.impl.channel

import com.felipebz.flr.api.TokenType

public object RegexpChannelBuilder {
    public const val DIGIT: String = "\\d"
    public const val ANY_CHAR: String = "[\\s\\S]"
    public const val OCTAL_DIGIT: String = "[0-7]"
    public const val HEXA_DIGIT: String = "[a-fA-F0-9]"

    @JvmStatic
    public fun regexp(type: TokenType, vararg regexpPiece: String): RegexpChannel {
        return RegexpChannel(type, merge(*regexpPiece))
    }

    @JvmStatic
    public fun commentRegexp(vararg regexpPiece: String): CommentRegexpChannel {
        return CommentRegexpChannel(merge(*regexpPiece))
    }

    @JvmStatic
    public fun opt(regexpPiece: String): String {
        return "$regexpPiece?+"
    }

    @JvmStatic
    public fun and(vararg regexpPieces: String?): String {
        val result = StringBuilder()
        for (rexpPiece in regexpPieces) {
            result.append(rexpPiece)
        }
        return result.toString()
    }

    @JvmStatic
    public fun one2n(regexpPiece: String): String {
        return "$regexpPiece++"
    }

    @JvmStatic
    public fun o2n(regexpPiece: String): String {
        return "$regexpPiece*+"
    }

    @JvmStatic
    public fun anyButNot(vararg character: String?): String {
        val result = StringBuilder()
        result.append("[^")
        for (element in character) {
            result.append(element)
        }
        result.append("]")
        return result.toString()
    }

    @JvmStatic
    public fun g(vararg regexpPiece: String?): String {
        val result = StringBuilder()
        result.append("(")
        for (element in regexpPiece) {
            result.append(element)
        }
        result.append(")")
        return result.toString()
    }

    @JvmStatic
    public fun or(vararg regexpPiece: String): String {
        val result = StringBuilder()
        result.append("(")
        for (i in regexpPiece.indices) {
            result.append(regexpPiece[i])
            if (i != regexpPiece.size - 1) {
                result.append("|")
            }
        }
        result.append(")")
        return result.toString()
    }

    @JvmStatic
    private fun merge(vararg piece: String): String {
        val result = StringBuilder()
        for (element in piece) {
            result.append(element)
        }
        return result.toString()
    }
}