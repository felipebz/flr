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
package com.sonar.sslr.test.minic.integration

import com.sonar.sslr.test.minic.MiniCParser
import org.apache.commons.io.FileUtils
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class MiniCOwnExamplesTest {
    @Test
    fun test() {
        val files = FileUtils.listFiles(File("src/test/resources/MiniCIntegration"), null, true)
        assertThat(files).isNotEmpty
        for (file in files) {
            try {
                parser.parse(file)
            } catch (e: RuntimeException) {
                e.printStackTrace()
                throw e
            }
        }
    }

    companion object {
        private val parser = MiniCParser.create()
    }
}