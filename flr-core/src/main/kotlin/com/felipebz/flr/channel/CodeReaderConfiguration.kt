//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.channel

/**
 * Configuration parameters used by a CodeReader to handle some specificities.
 */
public class CodeReaderConfiguration {
    private var tabWidth = DEFAULT_TAB_WIDTH
    private var codeReaderFilters = mutableListOf<CodeReaderFilter<*>>()

    /**
     * @return the tabWidth
     */
    public fun getTabWidth(): Int {
        return tabWidth
    }

    /**
     * @param tabWidth
     * the tabWidth to set
     */
    public fun setTabWidth(tabWidth: Int) {
        this.tabWidth = tabWidth
    }

    /**
     * @return the codeReaderFilters
     */
    public fun getCodeReaderFilters(): Array<CodeReaderFilter<*>> {
        return codeReaderFilters.toTypedArray()
    }

    /**
     * @param codeReaderFilters
     * the codeReaderFilters to set
     */
    public fun setCodeReaderFilters(vararg codeReaderFilters: CodeReaderFilter<*>) {
        this.codeReaderFilters = codeReaderFilters.toMutableList()
    }

    /**
     * Adds a code reader filter
     *
     * @param codeReaderFilter
     * the codeReaderFilter to add
     */
    public fun addCodeReaderFilters(codeReaderFilter: CodeReaderFilter<*>) {
        codeReaderFilters.add(codeReaderFilter)
    }

    public fun cloneWithoutCodeReaderFilters(): CodeReaderConfiguration {
        val clone = CodeReaderConfiguration()
        clone.setTabWidth(tabWidth)
        return clone
    }

    public companion object {
        public const val DEFAULT_TAB_WIDTH: Int = 1
    }
}
