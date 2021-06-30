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
package org.sonar.sslr.parser

import com.sonar.sslr.api.TokenType
import com.sonar.sslr.api.Trivia.TriviaKind
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.parser.GrammarOperators.commentTrivia
import org.sonar.sslr.parser.GrammarOperators.endOfInput
import org.sonar.sslr.parser.GrammarOperators.nextNot
import org.sonar.sslr.parser.GrammarOperators.nothing
import org.sonar.sslr.parser.GrammarOperators.oneOrMore
import org.sonar.sslr.parser.GrammarOperators.optional
import org.sonar.sslr.parser.GrammarOperators.regexp
import org.sonar.sslr.parser.GrammarOperators.sequence
import org.sonar.sslr.parser.GrammarOperators.skippedTrivia
import org.sonar.sslr.parser.GrammarOperators.token
import org.sonar.sslr.parser.GrammarOperators.zeroOrMore
import java.lang.reflect.Constructor

class GrammarOperatorsTest {
    @Test
    fun test() {
        val e1 = Mockito.mock(ParsingExpression::class.java)
        val e2 = Mockito.mock(ParsingExpression::class.java)
        Assertions.assertThat(sequence(e1)).isSameAs(e1)
        Assertions.assertThat(sequence(e1, e2)).isInstanceOf(SequenceExpression::class.java)
        Assertions.assertThat(sequence("foo")).isInstanceOf(StringExpression::class.java)
        Assertions.assertThat(sequence('f')).isInstanceOf(StringExpression::class.java)
        Assertions.assertThat(GrammarOperators.firstOf(e1)).isSameAs(e1)
        Assertions.assertThat(GrammarOperators.firstOf(e1, e2)).isInstanceOf(FirstOfExpression::class.java)
        Assertions.assertThat(optional(e1)).isInstanceOf(OptionalExpression::class.java)
        Assertions.assertThat(oneOrMore(e1)).isInstanceOf(OneOrMoreExpression::class.java)
        Assertions.assertThat(zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression::class.java)
        Assertions.assertThat(GrammarOperators.next(e1)).isInstanceOf(NextExpression::class.java)
        Assertions.assertThat(nextNot(e1)).isInstanceOf(NextNotExpression::class.java)
        Assertions.assertThat(regexp("foo")).isInstanceOf(PatternExpression::class.java)
        Assertions.assertThat(endOfInput()).isInstanceOf(EndOfInputExpression::class.java)
        Assertions.assertThat(nothing()).isInstanceOf(NothingExpression::class.java)
    }

    @Test
    fun test_token() {
        val tokenType = Mockito.mock(TokenType::class.java)
        val e = Mockito.mock(ParsingExpression::class.java)
        val result = token(tokenType, e)
        Assertions.assertThat(result).isInstanceOf(TokenExpression::class.java)
        Assertions.assertThat((result as TokenExpression).getTokenType()).isSameAs(tokenType)
    }

    @Test
    fun test_commentTrivia() {
        val e = Mockito.mock(ParsingExpression::class.java)
        val result = commentTrivia(e)
        Assertions.assertThat(result).isInstanceOf(TriviaExpression::class.java)
        Assertions.assertThat((result as TriviaExpression).getTriviaKind()).isEqualTo(TriviaKind.COMMENT)
    }

    @Test
    fun test_skippedTrivia() {
        val e = Mockito.mock(ParsingExpression::class.java)
        val result = skippedTrivia(e)
        Assertions.assertThat(result).isInstanceOf(TriviaExpression::class.java)
        Assertions.assertThat((result as TriviaExpression).getTriviaKind()).isEqualTo(TriviaKind.SKIPPED_TEXT)
    }

    @Test
    fun illegal_argument() {
        assertThrows("java.lang.Object", IllegalArgumentException::class.java) {
            sequence(Any())
        }
    }

    @Test
    @Throws(Exception::class)
    fun private_constructor() {
        val constructor: Constructor<*> = GrammarOperators::class.java.getDeclaredConstructor()
        Assertions.assertThat(constructor.isAccessible).isFalse()
        constructor.isAccessible = true
        constructor.newInstance()
    }
}