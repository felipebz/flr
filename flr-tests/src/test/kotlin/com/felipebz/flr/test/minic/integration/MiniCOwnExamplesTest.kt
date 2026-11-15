//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.test.minic.integration

import com.felipebz.flr.test.minic.MiniCParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class MiniCOwnExamplesTest {
    @Test
    fun test() {
        val files = File("src/test/resources/MiniCIntegration").listFiles().requireNoNulls()
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
