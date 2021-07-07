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

import com.sonar.sslr.impl.Parser
import java.nio.charset.Charset

/**
 * This class provides an default optimized implementation of the [ConfigurationModel] interface.
 *
 * It will call the [.doGetParser] and [.doGetTokenizers] methods only when a change
 * to the configuration has been made.
 *
 * @since 1.17
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

    override val parser: Parser<*>?
        get() {
            ensureUpToDate()
            return internalParser
        }

    /**
     * Gets a parser instance reflecting the current configuration state.
     * This method will not be called twice in a row without a change in the configuration state.
     *
     * @return A parser for the current configuration
     */
    public abstract fun doGetParser(): Parser<*>?
}