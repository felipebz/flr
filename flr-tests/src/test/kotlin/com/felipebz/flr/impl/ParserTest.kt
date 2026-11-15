//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl

import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.test.minic.MiniCParser.parseFile
import com.felipebz.flr.test.minic.MiniCParser.parseString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserTest {
    @Test
    fun lexerErrorStringWrappedInRecognitionException() {
        assertThrows<RecognitionException> {
            parseString(".")
        }
    }

    @Test
    fun lexerErrorFileWrappedInRecognitionException() {
        assertThrows<RecognitionException> {
            parseFile("/OwnExamples/lexererror.mc")
        }
    }

    @Test
    fun parse() {
        val compilationUnit = parseString("")
        assertThat(compilationUnit.numberOfChildren).isEqualTo(1)
        assertThat(checkNotNull(compilationUnit.firstChild).`is`(GenericTokenType.EOF)).isTrue()
    }
}
