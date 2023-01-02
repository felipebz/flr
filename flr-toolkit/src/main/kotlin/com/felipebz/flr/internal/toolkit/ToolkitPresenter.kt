/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.internal.toolkit

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.xpath.api.AstNodeXPathQuery.Companion.create
import com.felipebz.flr.toolkit.ConfigurationModel
import com.felipebz.flr.toolkit.ConfigurationProperty
import java.awt.Point
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.Thread.UncaughtExceptionHandler
import java.nio.file.Files
import java.nio.file.Paths

internal class ToolkitPresenter(private val configurationModel: ConfigurationModel, private val model: SourceCodeModel) {
    private lateinit var view: ToolkitView

    fun setView(view: ToolkitView) {
        this.view = view
    }

    fun checkInitialized() {
        check(::view.isInitialized) { "the view must be set before the presenter can be ran" }
    }

    fun initUncaughtExceptionsHandler() {
        Thread.currentThread().uncaughtExceptionHandler = UncaughtExceptionHandler { _, e ->
            val result: Writer = StringWriter()
            val printWriter = PrintWriter(result)
            e.printStackTrace(printWriter)
            view.appendToConsole(result.toString())
            view.setFocusOnConsoleView()
        }
    }

    fun initConfigurationTab() {
        for (configurationProperty in configurationModel.properties) {
            view.addConfigurationProperty(configurationProperty.name, configurationProperty.description)
            view.setConfigurationPropertyValue(configurationProperty.name, configurationProperty.value)
        }
    }

    fun run(title: String?) {
        checkInitialized()
        initUncaughtExceptionsHandler()
        view.setTitle(title)
        view.displaySourceCode("")
        view.displayAst(null)
        view.displayXml("")
        view.disableXPathEvaluateButton()
        initConfigurationTab()
        view.run()
    }

    fun onSourceCodeOpenButtonClick() {
        val fileToParse = view.pickFileToParse()
        if (fileToParse != null) {
            view.clearConsole()
            try {
                view.displaySourceCode(
                    String(
                        Files.readAllBytes(Paths.get(fileToParse.path)),
                        configurationModel.charset
                    )
                )
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            model.setSourceCode(fileToParse, configurationModel.charset)
            view.displaySourceCode(model.sourceCode)
            view.displayAst(model.astNode)
            view.displayXml(model.xml)
            view.scrollSourceCodeTo(Point(0, 0))
            view.setFocusOnAbstractSyntaxTreeView()
            view.enableXPathEvaluateButton()
        }
    }

    fun onSourceCodeParseButtonClick() {
        view.clearConsole()
        val sourceCode = view.sourceCode
        model.setSourceCode(sourceCode)
        val sourceCodeScrollbarPosition = view.sourceCodeScrollbarPosition
        view.displaySourceCode(model.sourceCode)
        view.displayAst(model.astNode)
        view.displayXml(model.xml)
        view.scrollSourceCodeTo(sourceCodeScrollbarPosition)
        view.setFocusOnAbstractSyntaxTreeView()
        view.enableXPathEvaluateButton()
    }

    fun onXPathEvaluateButtonClick() {
        val xpath = view.xPath ?: return
        val xpathQuery = create<Any>(xpath)
        view.clearConsole()
        view.clearAstSelections()
        view.clearSourceCodeHighlights()
        var firstAstNode: AstNode? = null
        for (resultObject in xpathQuery.selectNodes(model.astNode)) {
            if (resultObject is AstNode) {
                if (firstAstNode == null) {
                    firstAstNode = resultObject
                }
                view.selectAstNode(resultObject)
                view.highlightSourceCode(resultObject)
            }
        }
        view.scrollAstTo(firstAstNode)
        view.scrollSourceCodeTo(firstAstNode)
        view.setFocusOnAbstractSyntaxTreeView()
    }

    fun onSourceCodeKeyTyped() {
        view.displayAst(null)
        view.displayXml("")
        view.clearSourceCodeHighlights()
        view.disableXPathEvaluateButton()
    }

    fun onSourceCodeTextCursorMoved() {
        view.clearAstSelections()
        val astNode = view.astNodeFollowingCurrentSourceCodeTextCursorPosition
        view.selectAstNode(astNode)
        view.scrollAstTo(astNode)
    }

    fun onAstSelectionChanged() {
        view.clearSourceCodeHighlights()
        var firstAstNode: AstNode? = null
        for (astNode in view.selectedAstNodes) {
            if (firstAstNode == null) {
                firstAstNode = astNode
            }
            view.highlightSourceCode(astNode)
        }
        view.scrollSourceCodeTo(firstAstNode)
    }

    fun onConfigurationPropertyFocusLost(name: String) {
        val configurationProperty = requireNotNull(getConfigurationPropertyByName(name)) {
            "No such configuration property: $name"
        }
        val newValueCandidate = checkNotNull(view.getConfigurationPropertyValue(name))
        val errorMessage = configurationProperty.validate(newValueCandidate)
        view.setConfigurationPropertyErrorMessage(configurationProperty.name, errorMessage)
        if ("" == errorMessage) {
            configurationProperty.value = newValueCandidate
            configurationModel.setUpdatedFlag()
        } else {
            view.setFocusOnConfigurationPropertyField(name)
            view.setFocusOnConfigurationView()
        }
    }

    private fun getConfigurationPropertyByName(name: String): ConfigurationProperty? {
        for (configurationProperty in configurationModel.properties) {
            if (name == configurationProperty.name) {
                return configurationProperty
            }
        }
        return null
    }
}