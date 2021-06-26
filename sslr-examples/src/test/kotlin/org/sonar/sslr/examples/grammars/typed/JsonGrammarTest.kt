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
package org.sonar.sslr.examples.grammars.typed

import com.sonar.sslr.api.RecognitionException
import com.sonar.sslr.api.typed.ActionParser
import org.fest.assertions.Assertions
import org.junit.Test
import org.sonar.sslr.examples.grammars.typed.api.*
import java.nio.charset.StandardCharsets

class JsonGrammarTest {
    @Test
    @Throws(Exception::class)
    fun number() {
        assertLiteral("1234567890")
        assertLiteral("-1234567890")
        assertLiteral("0")
        assertLiteral("0.0123456789")
        assertLiteral("1E2")
        assertLiteral("1e2")
        assertLiteral("1E+2")
        assertLiteral("1E-2")
    }

    @Test(expected = RecognitionException::class)
    @Throws(Exception::class)
    fun number_not_parsed() {
        parser.parse("[ 01 ]")
    }

    @Test
    @Throws(Exception::class)
    fun string() {
        assertLiteral("\"\"")
        assertLiteral("\"\\\"\"")
        assertLiteral("\"\\\\\"")
        assertLiteral("\"\\/\"")
        assertLiteral("\"\\b\"")
        assertLiteral("\"\\f\"")
        assertLiteral("\"\\n\"")
        assertLiteral("\"\\r\"")
        assertLiteral("\"\\t\"")
        assertLiteral("\"\\uFFFF\"")
        assertLiteral("\"string\"")
    }

    @Test
    @Throws(Exception::class)
    fun value() {
        assertValue("\"string\"", LiteralTree::class.java)
        assertValue("{}", ObjectTree::class.java)
        assertValue("[]", ArrayTree::class.java)
        assertValue("42", LiteralTree::class.java)
        assertValue("true", BuiltInValueTree::class.java)
        assertValue("false", BuiltInValueTree::class.java)
        assertValue("null", BuiltInValueTree::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun `object`() {
        var tree = (parser.parse("{}") as JsonTree).arrayOrObject() as ObjectTree
        Assertions.assertThat(tree.openCurlyBraceToken().value()).isEqualTo("{")
        Assertions.assertThat(tree.closeCurlyBraceToken().value()).isEqualTo("}")
        Assertions.assertThat(tree.pairs()).isNull()
        tree = (parser.parse("{ \"string\" : true }") as JsonTree).arrayOrObject() as ObjectTree

        val pairs = tree.pairs()
        Assertions.assertThat(pairs).isNotNull
        checkNotNull(pairs)
        Assertions.assertThat(pairs.next()).isNull()
        val pair = checkNotNull(pairs.element())
        Assertions.assertThat(pair.name().token().value()).isEqualTo("\"string\"")
        Assertions.assertThat((pair.value() as BuiltInValueTree).token().value()).isEqualTo("true")
        Assertions.assertThat(pair.colonToken().value()).isEqualTo(":")
        parser.parse("{ \"string\" : true, \"string\" : false }")
    }

    @Test
    fun array() {
        var tree = (parser.parse("[]") as JsonTree).arrayOrObject() as ArrayTree
        Assertions.assertThat(checkNotNull(tree.openBracketToken()).value()).isEqualTo("[")
        Assertions.assertThat(checkNotNull(tree.closeBracketToken()).value()).isEqualTo("]")
        Assertions.assertThat(tree.values()).isNull()
        tree = (parser.parse("[ true, false ]") as JsonTree).arrayOrObject() as ArrayTree


        val values = tree.values()
        Assertions.assertThat(values).isNotNull
        checkNotNull(values)
        Assertions.assertThat(values).isNotNull
        Assertions.assertThat(values.element()).isInstanceOf(BuiltInValueTree::class.java)
        Assertions.assertThat(checkNotNull(values.commaToken()).value()).isEqualTo(",")
        Assertions.assertThat(checkNotNull(values.next()).element()).isInstanceOf(BuiltInValueTree::class.java)
    }

    @Test
    fun json() {
        var tree = parser.parse("{}")
        Assertions.assertThat(tree).isInstanceOf(JsonTree::class.java)
        Assertions.assertThat((tree as JsonTree).arrayOrObject()).isInstanceOf(ObjectTree::class.java)
        tree = parser.parse("[]")
        Assertions.assertThat(tree).isInstanceOf(JsonTree::class.java)
        Assertions.assertThat((tree as JsonTree).arrayOrObject()).isInstanceOf(ArrayTree::class.java)
    }

    private fun assertValue(code: String, c: Class<*>) {
        val tree = parser.parse("[ $code ]") as JsonTree
        val values = (tree.arrayOrObject() as ArrayTree).values()
        val value = checkNotNull(values).element()
        Assertions.assertThat(value).isInstanceOf(c)
    }

    private fun assertLiteral(code: String) {
        val tree = parser.parse("[ $code ]") as JsonTree
        val values = (tree.arrayOrObject() as ArrayTree).values()
        val value = checkNotNull(values).element()
        Assertions.assertThat(value).isInstanceOf(LiteralTree::class.java)
        Assertions.assertThat((value as LiteralTree).token().value()).isEqualTo(code)
    }

    companion object {
        private val parser = ActionParser<Tree>(
            StandardCharsets.UTF_8,
            JsonLexer.createGrammarBuilder(),
            JsonGrammar::class.java,
            TreeFactory(),
            JsonNodeBuilder(),
            JsonLexer.JSON
        )
    }
}