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

import org.sonar.sslr.internal.grammar.MutableParsingRule
import org.sonar.sslr.internal.vm.TokenExpression
import kotlin.math.min

object ParseTreePrinter {
    fun leafsToString(node: ParseNode, input: CharArray): String {
        val result = StringBuilder()
        printLeafs(node, input, result)
        return result.toString()
    }

    private fun printLeafs(node: ParseNode, input: CharArray, result: StringBuilder) {
        if (node.getChildren().isEmpty()) {
            for (i in node.getStartIndex() until min(node.getEndIndex(), input.size)) {
                result.append(input[i])
            }
        } else {
            for (child in node.getChildren()) {
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
        for (i in node.getStartIndex() until min(node.getEndIndex(), input.size)) {
            sb.append(input[i])
        }
        println(
            matcherToString(node.getMatcher())
                    + " (start=" + node.getStartIndex()
                    + ", end=" + node.getEndIndex()
                    + ", matches=" + sb.toString()
                    + ")"
        )
        for (child in node.getChildren()) {
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