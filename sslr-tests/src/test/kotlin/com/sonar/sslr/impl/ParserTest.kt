/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package com.sonar.sslr.impl

import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.RecognitionException
import com.sonar.sslr.test.minic.MiniCParser.parseFile
import com.sonar.sslr.test.minic.MiniCParser.parseString
import org.fest.assertions.Assertions
import org.junit.Test

class ParserTest {
    @Test(expected = RecognitionException::class)
    fun lexerErrorStringWrappedInRecognitionException() {
        parseString(".")
    }

    @Test(expected = RecognitionException::class)
    fun lexerErrorFileWrappedInRecognitionException() {
        parseFile("/OwnExamples/lexererror.mc")
    }

    @Test
    fun parse() {
        val compilationUnit = parseString("")
        Assertions.assertThat(compilationUnit.numberOfChildren).isEqualTo(1)
        Assertions.assertThat(checkNotNull(compilationUnit.firstChild).`is`(GenericTokenType.EOF)).isTrue()
    }
}