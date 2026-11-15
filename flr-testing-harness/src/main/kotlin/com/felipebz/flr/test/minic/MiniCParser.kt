//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.test.minic

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.Grammar
import com.felipebz.flr.impl.Parser
import java.io.File

public object MiniCParser {
    private val P = create()

    public fun create(): Parser<Grammar> {
        return Parser.builder(MiniCGrammar.create()).withLexer(MiniCLexer.create()).build()
    }


    public fun parseFile(filePath: String): AstNode {
        val file = File(MiniCParser::class.java.getResource(filePath).path)
        if (!file.exists()) {
            throw AssertionError("The file \"$filePath\" does not exist.")
        }
        return P.parse(file)
    }


    public fun parseString(source: String): AstNode {
        return P.parse(source)
    }
}
