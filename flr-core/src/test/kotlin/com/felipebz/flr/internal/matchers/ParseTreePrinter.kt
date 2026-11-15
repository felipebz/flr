//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
                matcher.name
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
