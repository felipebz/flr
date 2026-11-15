//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

import com.felipebz.flr.internal.toolkit.SourceCodeModel
import com.felipebz.flr.internal.toolkit.ToolkitPresenter
import com.felipebz.flr.internal.toolkit.ToolkitViewImpl
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * Creates a Toolkit with a title, and the given [ConfigurationModel].
 *
 * @param title
 * @param configurationModel
 */
public class Toolkit(private val title: String, private val configurationModel: ConfigurationModel) {
    public fun run() {
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
}
