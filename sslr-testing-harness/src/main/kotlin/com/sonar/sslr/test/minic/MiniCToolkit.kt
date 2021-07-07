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
package com.sonar.sslr.test.minic

import com.sonar.sslr.impl.Parser
import org.sonar.sslr.toolkit.AbstractConfigurationModel
import org.sonar.sslr.toolkit.ConfigurationProperty
import org.sonar.sslr.toolkit.Toolkit
import org.sonar.sslr.toolkit.ValidationCallback
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.UnsupportedCharsetException

public object MiniCToolkit {
    @JvmStatic
    public fun main(args: Array<String>) {
        val toolkit = Toolkit("SonarSource : MiniC : Toolkit", MiniCConfigurationModel())
        toolkit.run()
    }

    internal class MiniCConfigurationModel : AbstractConfigurationModel() {
        private val charsetProperty: ConfigurationProperty =
            ConfigurationProperty("Charset", "Charset used when opening files.", "UTF-8", object : ValidationCallback {
                override fun validate(newValueCandidate: String): String {
                    return try {
                        Charset.forName(newValueCandidate)
                        ""
                    } catch (e: IllegalCharsetNameException) {
                        "Illegal charset name: $newValueCandidate"
                    } catch (e: UnsupportedCharsetException) {
                        "Unsupported charset: $newValueCandidate"
                    }
                }
            })

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