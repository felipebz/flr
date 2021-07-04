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
package com.sonar.sslr.test.minic

import com.sonar.sslr.api.*
import com.sonar.sslr.impl.Parser
import java.io.File

object MiniCParser {
    private val P = create()
    @JvmStatic
    fun create(): Parser<Grammar> {
        return Parser.builder(MiniCGrammar.create()).withLexer(MiniCLexer.create()).build()
    }

    @JvmStatic
    fun parseFile(filePath: String): AstNode {
        val file = File(MiniCParser::class.java.getResource(filePath).path)
        if (!file.exists()) {
            throw AssertionError("The file \"$filePath\" does not exist.")
        }
        return P.parse(file)
    }

    @JvmStatic
    fun parseString(source: String): AstNode {
        return P.parse(source)
    }
}