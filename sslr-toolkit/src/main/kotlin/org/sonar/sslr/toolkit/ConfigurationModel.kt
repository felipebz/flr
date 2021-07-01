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
import java.nio.charset.Charset

/**
 * This interface is used to pass configuration properties to the Toolkit.
 *
 * The parser and tokenizer may depend on the configuration.
 * For example, a parser could depend on a charset configuration property.
 *
 * End-users should extend [AbstractConfigurationModel] instead of implementing this interface.
 *
 * @since 1.17
 */
interface ConfigurationModel {
    /**
     * Gets the properties to be shown, in the same order, in the Configuration tab.
     *
     * @return The list of configuration properties
     */
    val properties: List<ConfigurationProperty>

    /**
     * This method is called each time a configuration property's value is changed.
     */
    fun setUpdatedFlag()

    /**
     * Gets the character set reflecting the current configuration state.
     *
     * @return Charset for the current configuration
     *
     * @since 1.18
     */
    val charset: Charset

    /**
     * Gets a parser instance reflecting the current configuration state.
     *
     * @return A parser for the current configuration
     */
    val parser: Parser<*>?
}