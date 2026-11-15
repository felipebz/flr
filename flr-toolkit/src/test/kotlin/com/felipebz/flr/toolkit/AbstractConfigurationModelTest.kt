//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

import com.felipebz.flr.api.Grammar
import com.felipebz.flr.impl.Parser
import org.assertj.core.api.Assertions.assertThat
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
        private lateinit var internalParser: Parser<out Grammar>

        fun setParser(parser: Parser<out Grammar>) {
            internalParser = parser
        }

        override fun doGetParser(): Parser<*> {
            return internalParser
        }

        override val properties: List<ConfigurationProperty>
            get() {
                return emptyList()
            }
    }
}
