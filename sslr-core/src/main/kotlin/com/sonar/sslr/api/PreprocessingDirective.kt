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
package com.sonar.sslr.api

@Deprecated("in 1.20, use your own preprocessor API instead.")
abstract class PreprocessingDirective {
    abstract fun getAst(): AstNode?
    abstract fun getGrammar(): Grammar?
    private class DefaultPreprocessingDirective(private val astNode: AstNode?, private val grammar: Grammar?) :
        PreprocessingDirective() {
        override fun getAst(): AstNode? {
            return astNode
        }

        override fun getGrammar(): Grammar? {
            return grammar
        }
    }

    companion object {
        fun create(ast: AstNode?, grammar: Grammar?): PreprocessingDirective {
            return DefaultPreprocessingDirective(ast, grammar)
        }
    }
}