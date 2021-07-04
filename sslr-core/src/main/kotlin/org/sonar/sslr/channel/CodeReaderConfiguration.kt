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
package org.sonar.sslr.channel

import java.util.*

/**
 * Configuration parameters used by a CodeReader to handle some specificities.
 */
class CodeReaderConfiguration {
    private var tabWidth = DEFAULT_TAB_WIDTH
    private var codeReaderFilters = mutableListOf<CodeReaderFilter<*>>()

    /**
     * @return the tabWidth
     */
    fun getTabWidth(): Int {
        return tabWidth
    }

    /**
     * @param tabWidth
     * the tabWidth to set
     */
    fun setTabWidth(tabWidth: Int) {
        this.tabWidth = tabWidth
    }

    /**
     * @return the codeReaderFilters
     */
    fun getCodeReaderFilters(): Array<CodeReaderFilter<*>> {
        return codeReaderFilters.toTypedArray()
    }

    /**
     * @param codeReaderFilters
     * the codeReaderFilters to set
     */
    fun setCodeReaderFilters(vararg codeReaderFilters: CodeReaderFilter<*>?) {
        this.codeReaderFilters = ArrayList(listOf(*codeReaderFilters))
    }

    /**
     * Adds a code reader filter
     *
     * @param codeReaderFilter
     * the codeReaderFilter to add
     */
    fun addCodeReaderFilters(codeReaderFilter: CodeReaderFilter<*>) {
        codeReaderFilters.add(codeReaderFilter)
    }

    fun cloneWithoutCodeReaderFilters(): CodeReaderConfiguration {
        val clone = CodeReaderConfiguration()
        clone.setTabWidth(tabWidth)
        return clone
    }

    companion object {
        const val DEFAULT_TAB_WIDTH = 1
    }
}