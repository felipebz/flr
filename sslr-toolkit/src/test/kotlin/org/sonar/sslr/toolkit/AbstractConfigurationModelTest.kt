/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.sslr.toolkit

import com.sonar.sslr.api.Grammar
import com.sonar.sslr.impl.Parser
import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.sonar.colorizer.Tokenizer

class AbstractConfigurationModelTest {
    @Test
    fun parser_should_return_parser_instance() {
        val model = MyConfigurationModel()
        val p = mock(
            Parser::class.java
        )
        model.setParser(p)
        Assertions.assertThat(model.parser).isEqualTo(p)
    }

    @Test
    fun parser_should_return_same_parser_instance_when_flag_not_set() {
        val model = MyConfigurationModel()
        val p = mock(
            Parser::class.java
        )
        model.setParser(p)
        Assertions.assertThat(model.parser).isEqualTo(p)
        val p2 = mock(
            Parser::class.java
        )
        model.setParser(p2)
        Assertions.assertThat(model.parser).isEqualTo(p)
    }

    @Test
    fun parser_should_return_different_parser_instance_when_flag_set() {
        val model = MyConfigurationModel()
        val p = mock(
            Parser::class.java
        )
        model.setParser(p)
        Assertions.assertThat(model.parser).isEqualTo(p)
        val p2 = mock(
            Parser::class.java
        )
        model.setParser(p2)
        model.setUpdatedFlag()
        Assertions.assertThat(model.parser).isEqualTo(p2)
    }

    @Test
    fun tokenizers_should_return_parser_instance() {
        val model = MyConfigurationModel()
        val t: List<Tokenizer> = mock()
        model.setTokenizers(t)
        Assertions.assertThat(model.tokenizers).isEqualTo(t)
    }

    @Test
    fun tokenizers_should_return_same_parser_instance_when_flag_not_set() {
        val model = MyConfigurationModel()
        val t: List<Tokenizer> = mock()
        model.setTokenizers(t)
        Assertions.assertThat(model.tokenizers).isEqualTo(t)
        val t2: List<Tokenizer> = mock()
        model.setTokenizers(t2)
        Assertions.assertThat(model.tokenizers).isEqualTo(t)
    }

    @Test
    fun tokenizers_should_return_different_parser_instance_when_flag_set() {
        val model = MyConfigurationModel()
        val t: List<Tokenizer> = mock()
        model.setTokenizers(t)
        Assertions.assertThat(model.tokenizers).isEqualTo(t)
        val t2: List<Tokenizer> = mock()
        model.setTokenizers(t2)
        model.setUpdatedFlag()
        Assertions.assertThat(model.tokenizers).isEqualTo(t2)
    }

    private class MyConfigurationModel : AbstractConfigurationModel() {
        private var internalParser: Parser<out Grammar>? = null
        private var internalTokenizers: List<Tokenizer>? = null

        fun setParser(parser: Parser<out Grammar>?) {
            internalParser = parser
        }

        override fun doGetParser(): Parser<*>? {
            return internalParser
        }

        fun setTokenizers(tokenizers: List<Tokenizer>?) {
            internalTokenizers = tokenizers
        }

        override fun doGetTokenizers(): List<Tokenizer>? {
            return internalTokenizers
        }

        override val properties: List<ConfigurationProperty>
            get() {
                return emptyList()
            }
    }
}