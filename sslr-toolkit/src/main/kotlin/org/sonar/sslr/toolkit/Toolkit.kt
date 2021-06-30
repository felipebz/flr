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
package org.sonar.sslr.toolkit

import com.sonar.sslr.impl.Parser
import org.sonar.colorizer.Tokenizer
import org.sonar.sslr.internal.toolkit.SourceCodeModel
import org.sonar.sslr.internal.toolkit.ToolkitPresenter
import org.sonar.sslr.internal.toolkit.ToolkitViewImpl
import java.util.*
import javax.swing.SwingUtilities
import javax.swing.UIManager

class Toolkit(title: String?, configurationModel: ConfigurationModel) {
    private val title: String?
    private val configurationModel: ConfigurationModel

    /**
     * Create a Toolkit with a title, a static parser and static tokenizers.
     *
     * @param parser
     * @param tokenizers
     * @param title
     *
     */
    @Deprecated("in 1.17, use {@link #Toolkit(String, ConfigurationModel)} instead.")
    constructor(parser: Parser<*>, tokenizers: List<Tokenizer>?, title: String?) : this(
        title,
        object : AbstractConfigurationModel() {
            override val properties: List<ConfigurationProperty> = emptyList()

            override fun doGetTokenizers(): List<Tokenizer>? {
                return tokenizers
            }

            override fun doGetParser(): Parser<*> {
                return parser
            }
        })

    fun run() {
        SwingUtilities.invokeLater {
            try {
                for (info in UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus" == info.name) {
                        UIManager.setLookAndFeel(info.className)
                        break
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            val model = SourceCodeModel(configurationModel)
            val presenter = ToolkitPresenter(configurationModel, model)
            presenter.setView(ToolkitViewImpl(presenter))
            presenter.run(title)
        }
    }

    /**
     * Creates a Toolkit with a title, and the given [ConfigurationModel].
     *
     * @param title
     * @param configurationModel
     *
     * @since 1.17
     */
    init {
        Objects.requireNonNull(title)
        this.title = title
        this.configurationModel = configurationModel
    }
}