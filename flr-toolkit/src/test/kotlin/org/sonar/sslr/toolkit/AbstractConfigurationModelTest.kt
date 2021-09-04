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
package org.sonar.sslr.toolkit

import com.sonar.sslr.api.Grammar
import com.sonar.sslr.impl.Parser
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class AbstractConfigurationModelTest {
    @Test
    fun parser_should_return_parser_instance() {
        val model = MyConfigurationModel()
        val p = mock<Parser<Grammar>>()
        model.setParser(p)
        assertThat(model.parser).isEqualTo(p)
    }

    @Test
    fun parser_should_return_same_parser_instance_when_flag_not_set() {
        val model = MyConfigurationModel()
        val p = mock<Parser<Grammar>>()
        model.setParser(p)
        assertThat(model.parser).isEqualTo(p)
        val p2 = mock<Parser<Grammar>>()
        model.setParser(p2)
        assertThat(model.parser).isEqualTo(p)
    }

    @Test
    fun parser_should_return_different_parser_instance_when_flag_set() {
        val model = MyConfigurationModel()
        val p = mock<Parser<Grammar>>()
        model.setParser(p)
        assertThat(model.parser).isEqualTo(p)
        val p2 = mock<Parser<Grammar>>()
        model.setParser(p2)
        model.setUpdatedFlag()
        assertThat(model.parser).isEqualTo(p2)
    }

    private class MyConfigurationModel : AbstractConfigurationModel() {
        private var internalParser: Parser<out Grammar>? = null

        fun setParser(parser: Parser<out Grammar>?) {
            internalParser = parser
        }

        override fun doGetParser(): Parser<*>? {
            return internalParser
        }

        override val properties: List<ConfigurationProperty>
            get() {
                return emptyList()
            }
    }
}