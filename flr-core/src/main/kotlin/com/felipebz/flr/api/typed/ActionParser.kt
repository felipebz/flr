//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.api.typed

import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import com.felipebz.flr.impl.typed.GrammarBuilderInterceptor
import com.felipebz.flr.impl.typed.Interceptor
import com.felipebz.flr.impl.typed.MethodInterceptor
import com.felipebz.flr.impl.typed.ReflectionUtils.invokeMethod
import com.felipebz.flr.impl.typed.SyntaxTreeCreator
import com.felipebz.flr.parser.ParseErrorFormatter
import com.felipebz.flr.parser.ParseRunner
import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

public class ActionParser<N>(
    private val charset: Charset,
    b: LexerlessGrammarBuilder,
    grammarClass: Class<*>,
    treeFactory: Any,
    nodeBuilder: NodeBuilder,
    private val rootRule: GrammarRuleKey
) {
    private val syntaxTreeCreator: SyntaxTreeCreator<N>
    private val parseRunner: ParseRunner

    init {
        val grammarBuilderInterceptor: GrammarBuilderInterceptor<*> = GrammarBuilderInterceptor<Any?>(b)
        val treeFactoryInterceptor = Interceptor.create(
            treeFactory.javaClass, arrayOf(), arrayOf(),
            ActionMethodInterceptor(grammarBuilderInterceptor)
        )
        val grammar = Interceptor.create(
            grammarClass,
            arrayOf(GrammarBuilder::class.java, treeFactory.javaClass),
            arrayOf(grammarBuilderInterceptor, treeFactoryInterceptor),
            grammarBuilderInterceptor
        )
        for (method in grammarClass.methods) {
            if (method.declaringClass != Any::class.java) {
                invokeMethod(method, grammar)
            }
        }
        syntaxTreeCreator = SyntaxTreeCreator(treeFactory, grammarBuilderInterceptor, nodeBuilder)
        b.setRootRule(rootRule)
        parseRunner = ParseRunner(b.build().rootRule)
    }

    public fun parse(file: File): N? {
        return try {
            val chars = String(Files.readAllBytes(Paths.get(file.path)), charset).toCharArray()
            parse(Input(chars))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    public fun parse(source: String): N? {
        return parse(Input(source.toCharArray()))
    }

    private fun parse(input: Input): N? {
        val result = parseRunner.parse(input.input())
        if (!result.isMatched()) {
            val parseError = checkNotNull(result.getParseError())
            val inputBuffer = parseError.getInputBuffer()
            val position = inputBuffer.getPosition(parseError.getErrorIndex())
            val line = position.getLine()
            val column = position.getColumn()
            val message = ParseErrorFormatter().format(parseError)
            throw RecognitionException(line, column, message)
        }
        return syntaxTreeCreator.create(result.getParseTreeRoot(), input)
    }

    public fun rootRule(): GrammarRuleKey {
        return rootRule
    }

    private class ActionMethodInterceptor(private val grammarBuilderInterceptor: GrammarBuilderInterceptor<*>) :
        MethodInterceptor {
        override fun intercept(method: Method): Boolean {
            grammarBuilderInterceptor.addAction(method, method.parameterCount)
            return true
        }
    }
}
