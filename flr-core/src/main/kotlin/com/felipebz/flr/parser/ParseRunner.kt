//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.parser

import com.felipebz.flr.api.Rule
import com.felipebz.flr.internal.vm.CompilableGrammarRule
import com.felipebz.flr.internal.vm.CompiledGrammar
import com.felipebz.flr.internal.vm.Machine
import com.felipebz.flr.internal.vm.MutableGrammarCompiler
import com.felipebz.flr.profiler.ParsingProfiler

/**
 * Performs parsing of a given grammar rule on a given input text.
 *
 *
 * This class is not intended to be subclassed by clients.
 */
public class ParseRunner(rule: Rule) {
    private val compiledGrammar: CompiledGrammar = MutableGrammarCompiler.compile(rule as CompilableGrammarRule)

    public fun parse(input: CharArray): ParsingResult {
        return Machine.parse(input, compiledGrammar)
    }

    /**
     * Same as [parse], recording rule and memoization counters into [profiler].
     *
     * @since 1.7
     */
    public fun parse(input: CharArray, profiler: ParsingProfiler): ParsingResult {
        return Machine.parse(input, compiledGrammar, profiler.countersFor(compiledGrammar))
    }

}
