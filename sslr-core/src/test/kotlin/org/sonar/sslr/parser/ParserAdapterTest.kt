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
package org.sonar.sslr.parser

import com.sonar.sslr.api.RecognitionException
import com.sonar.sslr.api.Token
import com.sonar.sslr.impl.Parser.Companion.builder
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.sonar.sslr.internal.matchers.ExpressionGrammar
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class ParserAdapterTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()
    private lateinit var grammar: ExpressionGrammar
    private lateinit var parser: ParserAdapter<*>
    @Before
    fun setUp() {
        grammar = ExpressionGrammar()
        parser = ParserAdapter(Charset.forName("UTF-8"), grammar)
    }

    @Test
    fun should_return_grammar() {
        assertThat(parser.grammar).isSameAs(grammar)
    }

    @Test
    fun should_parse_string() {
        parser.parse("1+1")
    }

    @Test
    fun should_not_parse_invalid_string() {
        assertThrows("Parse error", RecognitionException::class.java) {
            parser.parse("")
        }
    }

    @Test
    @Throws(Exception::class)
    fun should_parse_file() {
        val file = temporaryFolder.newFile()
        file.writeText("1+1", StandardCharsets.UTF_8)
        parser.parse(file)
    }

    @Test
    fun should_not_parse_invalid_file() {
        val file = File("notfound")
        assertThrows(RecognitionException::class.java) {
            parser.parse(file)
        }
    }

    @Test
    fun builder_should_not_create_new_instance_from_adapter() {
        assertThat(builder(parser).build()).isSameAs(parser)
    }

    @Test
    fun parse_tokens_unsupported() {
        val tokens: List<Token> = listOf()
        assertThrows(UnsupportedOperationException::class.java) {
            parser.parse(tokens)
        }
    }

    @Test
    fun rootRule_unsupported() {
        assertThrows(UnsupportedOperationException::class.java) {
            parser.getRootRule()
        }
    }
}