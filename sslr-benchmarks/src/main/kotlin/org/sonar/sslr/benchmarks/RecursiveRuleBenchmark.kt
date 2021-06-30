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

import com.sonar.sslr.api.Grammar
import org.openjdk.jmh.annotations.*
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.grammar.LexerlessGrammarBuilder
import org.sonar.sslr.parser.ParseRunner
import org.sonar.sslr.parser.ParsingResult

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
open class RecursiveRuleBenchmark {
    private enum class MyGrammar : GrammarRuleKey {
        ROOT, RULE, TERM;

        companion object {
            fun recursive(): Grammar {
                val b = common()
                b.rule(RULE).`is`(TERM, b.optional("+", RULE))
                return b.build()
            }

            fun greedy(): Grammar {
                val b = common()
                b.rule(RULE).`is`(TERM, b.zeroOrMore("+", TERM))
                return b.build()
            }

            private fun common(): LexerlessGrammarBuilder {
                val b = LexerlessGrammarBuilder.create()
                b.setRootRule(ROOT)
                b.rule(ROOT).`is`(RULE, b.endOfInput())
                b.rule(TERM).`is`("t")
                return b
            }
        }
    }

    private lateinit var input: CharArray
    private lateinit var recursive: ParseRunner
    private lateinit var greedy: ParseRunner
    @Setup
    fun setup() {
        val n = Integer.getInteger("n", 3)
        input = ("t" + "+t".repeat(n)).toCharArray()
        val rootRule = checkNotNull(MyGrammar.recursive().getRootRule())
        recursive = ParseRunner(rootRule)
        greedy = ParseRunner(rootRule)
    }

    @Benchmark
    fun recursive(): ParsingResult {
        return recursive.parse(input)
    }

    @Benchmark
    fun greedy(): ParsingResult {
        return greedy.parse(input)
    }
}