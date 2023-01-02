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
package com.felipebz.flr.parser

import com.felipebz.flr.api.Rule
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.internal.vm.CompiledGrammar
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.MutableGrammarCompiler

/**
 * Performs parsing of a given grammar rule on a given input text.
 *
 *
 * This class is not intended to be subclassed by clients.
 *
 * @since 1.16
 */
public class ParseRunner(rule: Rule) {
    private val compiledGrammar: CompiledGrammar = MutableGrammarCompiler.compile(rule as CompilableGrammarRule)

    public fun parse(input: CharArray): ParsingResult {
        return Machine.parse(input, compiledGrammar)
    }

}