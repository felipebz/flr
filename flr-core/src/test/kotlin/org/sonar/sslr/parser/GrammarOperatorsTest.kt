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
package org.sonar.sslr.parser

import com.sonar.sslr.api.TokenType
import com.sonar.sslr.api.Trivia.TriviaKind
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.mock
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
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        assertThat(sequence(e1)).isSameAs(e1)
        assertThat(sequence(e1, e2)).isInstanceOf(SequenceExpression::class.java)
        assertThat(sequence("foo")).isInstanceOf(StringExpression::class.java)
        assertThat(sequence('f')).isInstanceOf(StringExpression::class.java)
        assertThat(GrammarOperators.firstOf(e1)).isSameAs(e1)
        assertThat(GrammarOperators.firstOf(e1, e2)).isInstanceOf(FirstOfExpression::class.java)
        assertThat(optional(e1)).isInstanceOf(OptionalExpression::class.java)
        assertThat(oneOrMore(e1)).isInstanceOf(OneOrMoreExpression::class.java)
        assertThat(zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression::class.java)
        assertThat(GrammarOperators.next(e1)).isInstanceOf(NextExpression::class.java)
        assertThat(nextNot(e1)).isInstanceOf(NextNotExpression::class.java)
        assertThat(regexp("foo")).isInstanceOf(PatternExpression::class.java)
        assertThat(endOfInput()).isInstanceOf(EndOfInputExpression::class.java)
        assertThat(nothing()).isInstanceOf(NothingExpression::class.java)
    }

    @Test
    fun test_token() {
        val tokenType = mock<TokenType>()
        val e = mock<ParsingExpression>()
        val result = token(tokenType, e)
        assertThat(result).isInstanceOf(TokenExpression::class.java)
        assertThat((result as TokenExpression).getTokenType()).isSameAs(tokenType)
    }

    @Test
    fun test_commentTrivia() {
        val e = mock<ParsingExpression>()
        val result = commentTrivia(e)
        assertThat(result).isInstanceOf(TriviaExpression::class.java)
        assertThat((result as TriviaExpression).getTriviaKind()).isEqualTo(TriviaKind.COMMENT)
    }

    @Test
    fun test_skippedTrivia() {
        val e = mock<ParsingExpression>()
        val result = skippedTrivia(e)
        assertThat(result).isInstanceOf(TriviaExpression::class.java)
        assertThat((result as TriviaExpression).getTriviaKind()).isEqualTo(TriviaKind.SKIPPED_TEXT)
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
        assertThat(constructor.isAccessible).isFalse()
        constructor.isAccessible = true
        constructor.newInstance()
    }
}