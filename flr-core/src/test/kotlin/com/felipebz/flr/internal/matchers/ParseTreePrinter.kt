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
package com.felipebz.flr.internal.matchers

import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.internal.vm.TokenExpression
import kotlin.math.min

object ParseTreePrinter {
    fun leafsToString(node: ParseNode, input: CharArray): String {
        val result = StringBuilder()
        printLeafs(node, input, result)
        return result.toString()
    }

    private fun printLeafs(node: ParseNode, input: CharArray, result: StringBuilder) {
        if (node.children.isEmpty()) {
            for (i in node.startIndex until min(node.endIndex, input.size)) {
                result.append(input[i])
            }
        } else {
            for (child in node.children) {
                printLeafs(child, input, result)
            }
        }
    }

    fun print(node: ParseNode, input: CharArray) {
        print(node, 0, input)
    }

    private fun print(node: ParseNode, level: Int, input: CharArray) {
        for (i in 0 until level) {
            print("  ")
        }
        val sb = StringBuilder()
        for (i in node.startIndex until min(node.endIndex, input.size)) {
            sb.append(input[i])
        }
        println(
            matcherToString(node.matcher)
                    + " (start=" + node.startIndex
                    + ", end=" + node.endIndex
                    + ", matches=" + sb.toString()
                    + ")"
        )
        for (child in node.children) {
            print(child, level + 1, input)
        }
    }

    private fun matcherToString(matcher: Matcher?): String {
        return when (matcher) {
            is MutableParsingRule -> {
                matcher.getName()
            }
            is TokenExpression -> {
                matcher.getTokenType().name
            }
            else -> {
                matcher.toString()
            }
        }
    }
}
