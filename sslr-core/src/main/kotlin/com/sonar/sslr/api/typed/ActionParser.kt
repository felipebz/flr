/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package com.sonar.sslr.api.typed

import com.sonar.sslr.api.RecognitionException
import com.sonar.sslr.impl.typed.GrammarBuilderInterceptor
import com.sonar.sslr.impl.typed.Interceptor
import com.sonar.sslr.impl.typed.MethodInterceptor
import com.sonar.sslr.impl.typed.ReflectionUtils.invokeMethod
import com.sonar.sslr.impl.typed.SyntaxTreeCreator
import org.sonar.sslr.grammar.GrammarRuleKey
import org.sonar.sslr.grammar.LexerlessGrammarBuilder
import org.sonar.sslr.parser.ParseErrorFormatter
import org.sonar.sslr.parser.ParseRunner
import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @since 1.21
 */
class ActionParser<N>(
    private val charset: Charset,
    b: LexerlessGrammarBuilder,
    grammarClass: Class<*>,
    treeFactory: Any,
    nodeBuilder: NodeBuilder,
    rootRule: GrammarRuleKey
) {
    private val syntaxTreeCreator: SyntaxTreeCreator<N>
    private val rootRule: GrammarRuleKey
    private val parseRunner: ParseRunner
    fun parse(file: File): N? {
        return try {
            val chars = String(Files.readAllBytes(Paths.get(file.path)), charset).toCharArray()
            parse(Input(chars, file.toURI()))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun parse(source: String): N? {
        return parse(Input(source.toCharArray()))
    }

    private fun parse(input: Input): N? {
        val result = parseRunner.parse(input.input())
        if (!result.isMatched()) {
            val parseError = checkNotNull(result.getParseError())
            val inputBuffer = parseError.getInputBuffer()
            val line = inputBuffer.getPosition(parseError.getErrorIndex()).getLine()
            val message = ParseErrorFormatter().format(parseError)
            throw RecognitionException(line, message)
        }
        return syntaxTreeCreator.create(result.getParseTreeRoot(), input)
    }

    fun rootRule(): GrammarRuleKey {
        return rootRule
    }

    private class ActionMethodInterceptor(private val grammarBuilderInterceptor: GrammarBuilderInterceptor<*>) :
        MethodInterceptor {
        override fun intercept(method: Method): Boolean {
            grammarBuilderInterceptor.addAction(method, method.parameterCount)
            return true
        }
    }

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
            if (method.declaringClass == Any::class.java) {
                continue
            }
            invokeMethod(method, grammar)
        }
        syntaxTreeCreator = SyntaxTreeCreator(treeFactory, grammarBuilderInterceptor, nodeBuilder)
        b.setRootRule(rootRule)
        this.rootRule = rootRule
        parseRunner = ParseRunner(checkNotNull(b.build().getRootRule()))
    }
}