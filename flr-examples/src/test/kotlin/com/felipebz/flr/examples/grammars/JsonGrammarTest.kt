//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars

import com.felipebz.flr.tests.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonGrammarTest {
    private val g = JsonGrammar.create()
    @Test
    fun whitespace() {
        assertThat(g.rule(JsonGrammar.WHITESPACE))
            .matches(" \n\r\t\u000C")
    }

    @Test
    fun number() {
        assertThat(g.rule(JsonGrammar.NUMBER))
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
        assertThat(g.rule(JsonGrammar.STRING))
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
        assertThat(g.rule(JsonGrammar.VALUE))
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
        assertThat(g.rule(JsonGrammar.OBJECT))
            .matches("{ }")
            .matches("{ \"string\" : true }")
            .matches("{ \"string\" : true, \"string\" : false }")
    }

    @Test
    fun array() {
        assertThat(g.rule(JsonGrammar.ARRAY))
            .matches("[ ]")
            .matches("[ true ]")
            .matches("[ true, false ]")
    }

    @Test
    fun json() {
        assertThat(g.rule(JsonGrammar.JSON))
            .matches("{}")
            .matches("[]")
    }
}
