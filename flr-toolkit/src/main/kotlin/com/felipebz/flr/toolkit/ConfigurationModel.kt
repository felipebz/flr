//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.toolkit

import com.felipebz.flr.impl.Parser
import java.nio.charset.Charset

/**
 * This interface is used to pass configuration properties to the Toolkit.
 *
 * The parser and tokenizer may depend on the configuration.
 * For example, a parser could depend on a charset configuration property.
 *
 * End-users should extend [AbstractConfigurationModel] instead of implementing this interface.
 */
public interface ConfigurationModel {
    /**
     * Gets the properties to be shown, in the same order, in the Configuration tab.
     *
     * @return The list of configuration properties
     */
    public val properties: List<ConfigurationProperty>

    /**
     * This method is called each time a configuration property's value is changed.
     */
    public fun setUpdatedFlag()

    /**
     * Gets the character set reflecting the current configuration state.
     *
     * @return Charset for the current configuration
     */
    public val charset: Charset

    /**
     * Gets a parser instance reflecting the current configuration state.
     *
     * @return A parser for the current configuration
     */
    public val parser: Parser<*>
}
