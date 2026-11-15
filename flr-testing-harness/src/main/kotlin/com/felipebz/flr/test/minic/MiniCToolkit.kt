//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.test.minic

import com.felipebz.flr.impl.Parser
import com.felipebz.flr.toolkit.AbstractConfigurationModel
import com.felipebz.flr.toolkit.ConfigurationProperty
import com.felipebz.flr.toolkit.Toolkit
import com.felipebz.flr.toolkit.Validators
import java.nio.charset.Charset

public fun main() {
    val toolkit = Toolkit("MiniC : Toolkit", MiniCToolkit.MiniCConfigurationModel())
    toolkit.run()
}

public object MiniCToolkit {
    internal class MiniCConfigurationModel : AbstractConfigurationModel() {
        private val charsetProperty: ConfigurationProperty =
            ConfigurationProperty("Charset", "Charset used when opening files.", "UTF-8", Validators.charsetValidator())

        override val properties: List<ConfigurationProperty>
            get() {
                return listOf(charsetProperty)
            }

        override val charset: Charset
            get() {
                return Charset.forName(charsetProperty.value)
            }

        override fun doGetParser(): Parser<*> {
            updateConfiguration()
            return MiniCParser.create()
        }

        companion object {
            private fun updateConfiguration() {
                /* Construct a parser configuration object from the properties */
            }
        }
    }
}
