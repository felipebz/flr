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
package org.sonar.sslr.examples.grammars

import org.junit.Test
import org.sonar.sslr.tests.Assertions

class JsonGrammarTest {
    private val g = JsonGrammar.create()
    @Test
    fun whitespace() {
        Assertions.assertThat(g.rule(JsonGrammar.WHITESPACE))
            .matches(" \n\r\t\u000C")
    }

    @Test
    fun number() {
        Assertions.assertThat(g.rule(JsonGrammar.NUMBER))
            .matches("1234567890")
            .matches("-1234567890")
            .matches("0")
            .notMatches("01")
            .matches("0.0123456789")
            .matches("1E2")
            .matches("1e2")
            .matches("1E+2")
            .matches("1E-2")
    }

    @Test
    fun string() {
        Assertions.assertThat(g.rule(JsonGrammar.STRING))
            .matches("\"\"")
            .matches("\"\\\"\"")
            .matches("\"\\\\\"")
            .matches("\"\\/\"")
            .matches("\"\\b\"")
            .matches("\"\\f\"")
            .matches("\"\\n\"")
            .matches("\"\\r\"")
            .matches("\"\\t\"")
            .matches("\"\\uFFFF\"")
            .matches("\"string\"")
    }

    @Test
    fun value() {
        Assertions.assertThat(g.rule(JsonGrammar.VALUE))
            .matches("\"string\"")
            .matches("{}")
            .matches("[]")
            .matches("42")
            .matches("true")
            .matches("false")
            .matches("null")
    }

    @Test
    fun `object`() {
        Assertions.assertThat(g.rule(JsonGrammar.OBJECT))
            .matches("{ }")
            .matches("{ \"string\" : true }")
            .matches("{ \"string\" : true, \"string\" : false }")
    }

    @Test
    fun array() {
        Assertions.assertThat(g.rule(JsonGrammar.ARRAY))
            .matches("[ ]")
            .matches("[ true ]")
            .matches("[ true, false ]")
    }

    @Test
    fun json() {
        Assertions.assertThat(g.rule(JsonGrammar.JSON))
            .matches("{}")
            .matches("[]")
    }
}