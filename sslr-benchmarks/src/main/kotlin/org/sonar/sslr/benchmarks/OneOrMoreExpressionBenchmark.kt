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
package org.sonar.sslr.benchmarks

import org.openjdk.jmh.annotations.*
import org.sonar.sslr.internal.vm.*

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
open class OneOrMoreExpressionBenchmark {
    private lateinit var input: String
    private lateinit var oneOrMore: Array<Instruction>
    private lateinit var usingZeroOrMore: Array<Instruction>
    @Setup
    fun setup() {
        val n = Integer.getInteger("n", 3)
        input = "t".repeat(n)
        val subExpression: ParsingExpression = StringExpression("t")
        oneOrMore = compile(OneOrMoreExpression(subExpression))
        usingZeroOrMore = compile(SequenceExpression(subExpression, ZeroOrMoreExpression(subExpression)))
    }

    @Benchmark
    fun oneOrMore(): Boolean {
        return Machine.execute(input, oneOrMore)
    }

    @Benchmark
    fun usingZeroOrMore(): Boolean {
        return Machine.execute(input, usingZeroOrMore)
    }

    companion object {
        private fun compile(expression: ParsingExpression): Array<Instruction> {
            return SequenceExpression(expression, EndOfInputExpression.INSTANCE).compile(CompilationHandler())
        }
    }
}