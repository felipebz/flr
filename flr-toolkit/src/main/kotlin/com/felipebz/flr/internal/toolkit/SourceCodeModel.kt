//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.toolkit

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.impl.ast.AstXmlPrinter
import com.felipebz.flr.toolkit.ConfigurationModel
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

internal class SourceCodeModel(private val configurationModel: ConfigurationModel) {
    lateinit var sourceCode: String
        private set
    lateinit var astNode: AstNode
        private set

    fun setSourceCode(source: File, charset: Charset) {
        astNode = configurationModel.parser.parse(source)
        try {
            sourceCode = String(Files.readAllBytes(Paths.get(source.path)), charset)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun setSourceCode(sourceCode: String) {
        astNode = configurationModel.parser.parse(sourceCode)
        this.sourceCode = sourceCode
    }

    val xml: String
        get() = AstXmlPrinter.print(astNode)
}
