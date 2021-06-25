/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.sslr.internal.toolkit

import com.google.common.collect.ImmutableList
import com.sonar.sslr.api.AstNode
import com.sonar.sslr.api.GenericTokenType
import com.sonar.sslr.api.Token
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.sonar.sslr.toolkit.ConfigurationModel
import org.sonar.sslr.toolkit.ConfigurationProperty
import java.awt.Point
import java.io.File
import java.io.PrintWriter
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class ToolkitPresenterTest {
    @Test
    fun checkInitializedBad() {
        assertThrows("the view must be set before the presenter can be ran", IllegalStateException::class.java) {
            val presenter = ToolkitPresenter(
                Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                    SourceCodeModel::class.java
                )
            )
            presenter.checkInitialized()
        }
    }

    @Test
    fun checkInitializedGood() {
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(Mockito.mock(ToolkitView::class.java))
        presenter.checkInitialized()
    }

    @Test
    @Throws(InterruptedException::class)
    fun initUncaughtExceptionsHandler() {
        val view = Mockito.mock(ToolkitView::class.java)
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.initUncaughtExceptionsHandler()
        val uncaughtExceptionHandler = Thread.currentThread().uncaughtExceptionHandler
        Assertions.assertThat(uncaughtExceptionHandler is ThreadGroup).isFalse()
        val e = Mockito.mock(Throwable::class.java)
        uncaughtExceptionHandler.uncaughtException(null, e)
        Mockito.verify(e).printStackTrace(ArgumentMatchers.any(PrintWriter::class.java))
        Mockito.verify(view).appendToConsole(ArgumentMatchers.anyString())
        Mockito.verify(view).setFocusOnConsoleView()
    }

    @Test
    fun initConfigurationTab() {
        val view = Mockito.mock(ToolkitView::class.java)
        var presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.initConfigurationTab()
        Mockito.verify(view, Mockito.never()).addConfigurationProperty(Mockito.anyString(), Mockito.anyString())
        Mockito.verify(view, Mockito.never()).setConfigurationPropertyValue(Mockito.anyString(), Mockito.anyString())
        val property1 = Mockito.mock(ConfigurationProperty::class.java)
        Mockito.`when`(property1.name).thenReturn("property1")
        Mockito.`when`(property1.description).thenReturn("description1")
        Mockito.`when`(property1.value).thenReturn("default1")
        val property2 = Mockito.mock(ConfigurationProperty::class.java)
        Mockito.`when`(property2.name).thenReturn("property2")
        Mockito.`when`(property2.description).thenReturn("description2")
        Mockito.`when`(property2.value).thenReturn("default2")
        val configurationModel = Mockito.mock(ConfigurationModel::class.java)
        Mockito.`when`(configurationModel.properties).thenReturn(ImmutableList.of(property1, property2))
        presenter = ToolkitPresenter(configurationModel, Mockito.mock(SourceCodeModel::class.java))
        presenter.setView(view)
        presenter.initConfigurationTab()
        Mockito.verify(view).addConfigurationProperty("property1", "description1")
        Mockito.verify(view).setConfigurationPropertyValue("property1", "default1")
        Mockito.verify(view).addConfigurationProperty("property2", "description2")
        Mockito.verify(view).setConfigurationPropertyValue("property2", "default2")
    }

    @Test
    fun run() {
        val view = Mockito.mock(ToolkitView::class.java)
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.run("my_mocked_title")
        Assertions.assertThat(Thread.currentThread().uncaughtExceptionHandler is ThreadGroup).isFalse()
        Mockito.verify(view).setTitle("my_mocked_title")
        Mockito.verify(view).displayHighlightedSourceCode("")
        Mockito.verify(view).displayAst(null)
        Mockito.verify(view).displayXml("")
        Mockito.verify(view).disableXPathEvaluateButton()
        Mockito.verify(view).run()
    }

    @Test
    fun run_should_call_initConfigurationTab() {
        val view = Mockito.mock(ToolkitView::class.java)
        var presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.run("my_mocked_title")
        Mockito.verify(view, Mockito.never()).addConfigurationProperty(Mockito.anyString(), Mockito.anyString())
        val configurationModel = Mockito.mock(ConfigurationModel::class.java)
        Mockito.`when`(configurationModel.properties).thenReturn(
            listOf(ConfigurationProperty("", "", ""))
        )
        presenter = ToolkitPresenter(configurationModel, Mockito.mock(SourceCodeModel::class.java))
        presenter.setView(view)
        presenter.run("my_mocked_title")
        Mockito.verify(view).addConfigurationProperty(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun runFailsWithoutView() {
        assertThrows(IllegalStateException::class.java) {
            ToolkitPresenter(
                Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                    SourceCodeModel::class.java
                )
            ).run("foo")
        }
    }

    @Test
    fun onSourceCodeOpenButtonClick() {
        val view = Mockito.mock(ToolkitView::class.java)
        val file = File("src/test/resources/parse_error.txt")
        Mockito.`when`(view.pickFileToParse()).thenReturn(file)
        val model = Mockito.mock(SourceCodeModel::class.java)
        val astNode = Mockito.mock(AstNode::class.java)
        Mockito.`when`(model.highlightedSourceCode).thenReturn("my_mocked_highlighted_source_code")
        Mockito.`when`(model.astNode).thenReturn(astNode)
        Mockito.`when`(model.xml).thenReturn("my_mocked_xml")
        val presenter = ToolkitPresenter(
            (Mockito.`when`(
                Mockito.mock(
                    ConfigurationModel::class.java
                ).charset
            ).thenReturn(StandardCharsets.UTF_8).getMock<Any>() as ConfigurationModel), model
        )
        presenter.setView(view)
        presenter.onSourceCodeOpenButtonClick()
        Mockito.verify(view).pickFileToParse()
        Mockito.verify(view).clearConsole()
        Mockito.verify(view).displayHighlightedSourceCode("my_mocked_highlighted_source_code")
        Mockito.verify(model).setSourceCode(file, StandardCharsets.UTF_8)
        Mockito.verify(view).displayAst(astNode)
        Mockito.verify(view).displayXml("my_mocked_xml")
        Mockito.verify(view).scrollSourceCodeTo(Point(0, 0))
        Mockito.verify(view).setFocusOnAbstractSyntaxTreeView()
        Mockito.verify(view).enableXPathEvaluateButton()
    }

    @Test
    fun onSourceCodeOpenButtonClick_with_parse_error_should_clear_console_and_display_code() {
        val view = Mockito.mock(ToolkitView::class.java)
        val file = File("src/test/resources/parse_error.txt")
        Mockito.`when`(view.pickFileToParse()).thenReturn(file)
        val model = Mockito.mock(SourceCodeModel::class.java)
        Mockito.doThrow(RuntimeException("Parse error")).`when`(model).setSourceCode(
            any(), Mockito.any(Charset::class.java)
        )
        val presenter = ToolkitPresenter(
            (Mockito.`when`(
                Mockito.mock(
                    ConfigurationModel::class.java
                ).charset
            ).thenReturn(StandardCharsets.UTF_8).getMock<Any>() as ConfigurationModel), model
        )
        presenter.setView(view)
        try {
            presenter.onSourceCodeOpenButtonClick()
            throw AssertionError("Expected an exception")
        } catch (e: RuntimeException) {
            Mockito.verify(view).clearConsole()
            Mockito.verify(view).displayHighlightedSourceCode("parse_error.txt")
        }
    }

    @Test
    fun onSourceCodeOpenButtonClick_should_no_operation_when_no_file() {
        val view = Mockito.mock(ToolkitView::class.java)
        Mockito.`when`(view.pickFileToParse()).thenReturn(null)
        val model = Mockito.mock(SourceCodeModel::class.java)
        val presenter = ToolkitPresenter(Mockito.mock(ConfigurationModel::class.java), model)
        presenter.setView(view)
        presenter.onSourceCodeOpenButtonClick()
        Mockito.verify(view).pickFileToParse()
        Mockito.verify(view, Mockito.never()).clearConsole()
        Mockito.verify(model, Mockito.never()).setSourceCode(any(), any())
        Mockito.verify(view, Mockito.never()).displayHighlightedSourceCode(ArgumentMatchers.anyString())
        Mockito.verify(view, Mockito.never()).displayAst(any())
        Mockito.verify(view, Mockito.never()).displayXml(ArgumentMatchers.anyString())
        Mockito.verify(view, Mockito.never()).scrollSourceCodeTo(any<Point>())
        Mockito.verify(view, Mockito.never()).enableXPathEvaluateButton()
    }

    @Test
    fun onSourceCodeParseButtonClick() {
        val view = Mockito.mock(ToolkitView::class.java)
        Mockito.`when`(view.sourceCode).thenReturn("my_mocked_source")
        val point = Mockito.mock(Point::class.java)
        Mockito.`when`(view.sourceCodeScrollbarPosition).thenReturn(point)
        val model = Mockito.mock(SourceCodeModel::class.java)
        Mockito.`when`(model.highlightedSourceCode).thenReturn("my_mocked_highlighted_source_code")
        val astNode = Mockito.mock(AstNode::class.java)
        Mockito.`when`(model.astNode).thenReturn(astNode)
        Mockito.`when`(model.xml).thenReturn("my_mocked_xml")
        val presenter = ToolkitPresenter(Mockito.mock(ConfigurationModel::class.java), model)
        presenter.setView(view)
        presenter.onSourceCodeParseButtonClick()
        Mockito.verify(view).clearConsole()
        Mockito.verify(view).sourceCode
        Mockito.verify(model).setSourceCode("my_mocked_source")
        Mockito.verify(view).displayHighlightedSourceCode("my_mocked_highlighted_source_code")
        view.displayAst(astNode)
        view.displayXml("my_mocked_xml")
        view.scrollSourceCodeTo(point)
        Mockito.verify(view).setFocusOnAbstractSyntaxTreeView()
        Mockito.verify(view).enableXPathEvaluateButton()
    }

    @Test
    fun onXPathEvaluateButtonClickAstNodeResults() {
        val view = Mockito.mock(ToolkitView::class.java)
        Mockito.`when`(view.xPath).thenReturn("//foo")
        val model = Mockito.mock(SourceCodeModel::class.java)
        val astNode = AstNode(GenericTokenType.IDENTIFIER, "foo", null)
        Mockito.`when`(model.astNode).thenReturn(astNode)
        val presenter = ToolkitPresenter(Mockito.mock(ConfigurationModel::class.java), model)
        presenter.setView(view)
        presenter.onXPathEvaluateButtonClick()
        Mockito.verify(view).clearAstSelections()
        Mockito.verify(view).clearSourceCodeHighlights()
        Mockito.verify(view).selectAstNode(astNode)
        Mockito.verify(view).highlightSourceCode(astNode)
        Mockito.verify(view).scrollAstTo(astNode)
    }

    @Test
    fun onXPathEvaluateButtonClickScrollToFirstAstNode() {
        val view = Mockito.mock(ToolkitView::class.java)
        Mockito.`when`(view.xPath).thenReturn("//foo")
        val model = Mockito.mock(SourceCodeModel::class.java)
        val astNode = AstNode(GenericTokenType.IDENTIFIER, "foo", null)
        val childAstNode = AstNode(GenericTokenType.IDENTIFIER, "foo", null)
        astNode.addChild(childAstNode)
        Mockito.`when`(model.astNode).thenReturn(astNode)
        val presenter = ToolkitPresenter(Mockito.mock(ConfigurationModel::class.java), model)
        presenter.setView(view)
        presenter.onXPathEvaluateButtonClick()
        Mockito.verify(view).scrollAstTo(astNode)
        Mockito.verify(view, Mockito.never()).scrollAstTo(childAstNode)
        Mockito.verify(view).scrollSourceCodeTo(astNode)
        Mockito.verify(view, Mockito.never()).scrollSourceCodeTo(childAstNode)
    }

    @Test
    @Throws(Exception::class)
    fun onXPathEvaluateButtonClickStringResult() {
        val view = Mockito.mock(ToolkitView::class.java)
        Mockito.`when`(view.xPath).thenReturn("//foo/@tokenValue")
        val model = Mockito.mock(SourceCodeModel::class.java)
        val token = Token.builder()
            .setType(GenericTokenType.IDENTIFIER)
            .setValueAndOriginalValue("bar")
            .setURI(URI("tests://unittest"))
            .setLine(1)
            .setColumn(1)
            .build()
        val astNode = AstNode(GenericTokenType.IDENTIFIER, "foo", token)
        Mockito.`when`(model.astNode).thenReturn(astNode)
        val presenter = ToolkitPresenter(Mockito.mock(ConfigurationModel::class.java), model)
        presenter.setView(view)
        presenter.onXPathEvaluateButtonClick()
        Mockito.verify(view).clearConsole()
        Mockito.verify(view).clearAstSelections()
        Mockito.verify(view).clearSourceCodeHighlights()
        Mockito.verify(view, Mockito.never()).selectAstNode(any())
        Mockito.verify(view, Mockito.never()).highlightSourceCode(any())
        Mockito.verify(view).scrollAstTo(null)
        Mockito.verify(view).scrollSourceCodeTo(null as AstNode?)
        Mockito.verify(view).setFocusOnAbstractSyntaxTreeView()
    }

    @Test
    fun onSourceCodeKeyTyped() {
        val view = Mockito.mock(ToolkitView::class.java)
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.onSourceCodeKeyTyped()
        Mockito.verify(view).displayAst(null)
        Mockito.verify(view).displayXml("")
        Mockito.verify(view).clearSourceCodeHighlights()
        Mockito.verify(view).disableXPathEvaluateButton()
    }

    @Test
    fun onSourceCodeTextCursorMoved() {
        val view = Mockito.mock(ToolkitView::class.java)
        val astNode = Mockito.mock(AstNode::class.java)
        Mockito.`when`(view.astNodeFollowingCurrentSourceCodeTextCursorPosition).thenReturn(astNode)
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.onSourceCodeTextCursorMoved()
        Mockito.verify(view).clearAstSelections()
        Mockito.verify(view).selectAstNode(astNode)
        Mockito.verify(view).scrollAstTo(astNode)
    }

    @Test
    fun onAstSelectionChanged() {
        val view = Mockito.mock(ToolkitView::class.java)
        val firstAstNode = Mockito.mock(AstNode::class.java)
        val secondAstNode = Mockito.mock(AstNode::class.java)
        Mockito.`when`(view.selectedAstNodes).thenReturn(listOf(firstAstNode, secondAstNode))
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.onAstSelectionChanged()
        Mockito.verify(view).clearSourceCodeHighlights()
        Mockito.verify(view).highlightSourceCode(firstAstNode)
        Mockito.verify(view).highlightSourceCode(secondAstNode)
        Mockito.verify(view).scrollSourceCodeTo(firstAstNode)
        Mockito.verify(view, Mockito.never()).scrollSourceCodeTo(secondAstNode)
    }

    @Test
    fun onConfigurationPropertyFocusLost_when_validation_successes() {
        val view = Mockito.mock(ToolkitView::class.java)
        val property = Mockito.mock(ConfigurationProperty::class.java)
        Mockito.`when`(property.name).thenReturn("name")
        Mockito.`when`(property.description).thenReturn("description")
        Mockito.`when`(view.getConfigurationPropertyValue("name")).thenReturn("foo")
        Mockito.`when`(property.validate("foo")).thenReturn("")
        val configurationModel = Mockito.mock(ConfigurationModel::class.java)
        Mockito.`when`(configurationModel.properties).thenReturn(ImmutableList.of(property))
        val presenter = ToolkitPresenter(
            configurationModel, Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.onConfigurationPropertyFocusLost("name")
        Mockito.verify(view).setConfigurationPropertyErrorMessage("name", "")
        Mockito.verify(view, Mockito.never()).setFocusOnConfigurationPropertyField(Mockito.anyString())
        Mockito.verify(view, Mockito.never()).setFocusOnConfigurationView()
        Mockito.verify(property).value = "foo"
        Mockito.verify(configurationModel).setUpdatedFlag()
    }

    @Test
    fun onConfigurationPropertyFocusLost_when_validation_fails() {
        val view = Mockito.mock(ToolkitView::class.java)
        val property = Mockito.mock(ConfigurationProperty::class.java)
        Mockito.`when`(property.name).thenReturn("name")
        Mockito.`when`(property.description).thenReturn("description")
        Mockito.`when`(view.getConfigurationPropertyValue("name")).thenReturn("foo")
        Mockito.`when`(property.validate("foo")).thenReturn("The value foo is forbidden!")
        val configurationModel = Mockito.mock(ConfigurationModel::class.java)
        Mockito.`when`(configurationModel.properties).thenReturn(ImmutableList.of(property))
        val presenter = ToolkitPresenter(
            configurationModel, Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        presenter.onConfigurationPropertyFocusLost("name")
        Mockito.verify(view).setConfigurationPropertyErrorMessage("name", "The value foo is forbidden!")
        Mockito.verify(view).setFocusOnConfigurationPropertyField("name")
        Mockito.verify(view).setFocusOnConfigurationView()
        Mockito.verify(property, Mockito.never()).value = "foo"
        Mockito.verify(configurationModel, Mockito.never()).setUpdatedFlag()
    }

    @Test
    fun onConfigurationPropertyFocusLost_with_invalid_name() {
        val view = Mockito.mock(ToolkitView::class.java)
        val presenter = ToolkitPresenter(
            Mockito.mock(ConfigurationModel::class.java), Mockito.mock(
                SourceCodeModel::class.java
            )
        )
        presenter.setView(view)
        assertThrows("No such configuration property: name", IllegalArgumentException::class.java) {
            presenter.onConfigurationPropertyFocusLost("name")
        }
    }
}