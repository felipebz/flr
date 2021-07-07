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
package org.sonar.sslr.internal.toolkit

import com.sonar.sslr.api.AstNode
import com.sonar.sslr.impl.ast.AstXmlPrinter
import org.sonar.sslr.toolkit.ConfigurationModel
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

public class SourceCodeModel(configurationModel: ConfigurationModel) {
    private val configurationModel: ConfigurationModel
    public lateinit var sourceCode: String
        private set
    public lateinit var astNode: AstNode
        private set

    public fun setSourceCode(source: File, charset: Charset?) {
        astNode = configurationModel.parser!!.parse(source)
        try {
            sourceCode = String(Files.readAllBytes(Paths.get(source.path)), charset!!)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    public fun setSourceCode(sourceCode: String?) {
        astNode = configurationModel.parser!!.parse(sourceCode!!)
        this.sourceCode = sourceCode
    }

    public val xml: String
        get() = AstXmlPrinter.print(astNode)

    init {
        Objects.requireNonNull(configurationModel)
        this.configurationModel = configurationModel
    }
}