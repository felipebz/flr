//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

import com.felipebz.flr.impl.Parser
import java.nio.charset.Charset

/**
 * This class provides an default optimized implementation of the [ConfigurationModel] interface.
 *
 * It will call the [.doGetParser] and [.doGetTokenizers] methods only when a change
 * to the configuration has been made.
 */
public abstract class AbstractConfigurationModel : ConfigurationModel {
    private var updatedFlag = true
    private var internalParser: Parser<*>? = null

    override fun setUpdatedFlag() {
        updatedFlag = true
    }

    private fun ensureUpToDate() {
        if (updatedFlag) {
            internalParser = doGetParser()
        }
        updatedFlag = false
    }

    /**
     * Gets the charset reflecting the current configuration state.
     *
     * @return Charset for the current configuration
     */
    override val charset: Charset
        get() {
            return Charset.defaultCharset()
        }

    override val parser: Parser<*>
        get() {
            ensureUpToDate()
            return internalParser ?: throw IllegalStateException("Parser not initialized")
        }

    /**
     * Gets a parser instance reflecting the current configuration state.
     * This method will not be called twice in a row without a change in the configuration state.
     *
     * @return A parser for the current configuration
     */
    public abstract fun doGetParser(): Parser<*>
}
