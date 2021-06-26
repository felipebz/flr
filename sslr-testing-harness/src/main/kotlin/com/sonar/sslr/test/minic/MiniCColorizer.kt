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
package com.sonar.sslr.test.minic

import org.sonar.colorizer.CDocTokenizer
import org.sonar.colorizer.CppDocTokenizer
import org.sonar.colorizer.KeywordsTokenizer
import org.sonar.colorizer.Tokenizer

object MiniCColorizer {
    fun getTokenizers(): List<Tokenizer> {
        return listOf(
            CDocTokenizer("<span class=\"cd\">", "</span>"),
            CppDocTokenizer("<span class=\"cppd\">", "</span>"),
            KeywordsTokenizer("<span class=\"k\">", "</span>", *MiniCLexer.Keywords.keywordValues())
        )
    }
}