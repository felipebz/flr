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
package org.sonar.sslr.benchmarks

import org.openjdk.jmh.annotations.*
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.grammar.LexerlessGrammarBuilder
import org.sonar.sslr.parser.ParseRunner

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
open class MemoizationOfMismatchesBenchmark {
    private lateinit var required: ParseRunner
    private lateinit var notRequired: ParseRunner
    private lateinit var input: CharArray
    @Setup
    fun setup() {
        val k = Integer.getInteger("k", 5)
        val n = Integer.getInteger("n", 10)
        input = (" k" + (k - 1)).repeat(n).toCharArray()
        val root = newRuleKey()
        val spacing = newRuleKey()
        val rules = (0 until k).map { newRuleKey() }.toTypedArray()
        var b = LexerlessGrammarBuilder.create()
        b.rule(root).`is`(
            b.zeroOrMore(b.firstOf(rules[0], rules[1], *rules.copyOfRange(2, rules.size))),
            b.endOfInput()
        )
        b.rule(spacing).`is`(" ")
        for (i in 0 until k) {
            b.rule(rules[i]).`is`(b.optional(spacing), "k$i")
        }
        required = ParseRunner(b.build().rule(root))
        b = LexerlessGrammarBuilder.create()
        b.rule(root).`is`(
            b.zeroOrMore(b.firstOf(rules[0], rules[1], *rules.copyOfRange(2, rules.size))),
            b.endOfInput()
        )
        b.rule(spacing).`is`(b.optional(" "))
        for (i in 0 until k) {
            b.rule(rules[i]).`is`(spacing, "k$i")
        }
        notRequired = ParseRunner(b.build().rule(root))
    }

    @Benchmark
    fun required(): Boolean {
        return required.parse(input).isMatched()
    }

    @Benchmark
    fun notRequired(): Boolean {
        return notRequired.parse(input).isMatched()
    }

    companion object {
        private fun newRuleKey(): GrammarRuleKey {
            return object : GrammarRuleKey {}
        }
    }
}