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
package com.sonar.sslr.impl.matcher

import com.sonar.sslr.api.TokenType
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.adjacent
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.anyTokenButNot
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.bridge
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.exclusiveTill
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.isFalse
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.isOneOfThem
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.isTrue
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.till
import com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.tillNewLine
import com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.not
import com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and
import com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n
import com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.one2n
import com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt
import com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or
import com.sonar.sslr.impl.matcher.RuleDefinition
import org.fest.assertions.Assertions
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.internal.vm.lexerful.*

class GrammarFunctionsTest {
    @Test
    fun test() {
        val e1 = Mockito.mock(ParsingExpression::class.java)
        val e2 = Mockito.mock(ParsingExpression::class.java)
        val rule = Mockito.mock(RuleDefinition::class.java)
        Assertions.assertThat(and(rule)).isSameAs(rule)
        Assertions.assertThat(and(e1)).isSameAs(e1)
        Assertions.assertThat(and(e1, e2)).isInstanceOf(SequenceExpression::class.java)
        Assertions.assertThat(and("foo")).isInstanceOf(
            TokenValueExpression::class.java
        )
        Assertions.assertThat(
            and(
                Mockito.mock(
                    TokenType::class.java
                )
            )
        ).isInstanceOf(TokenTypeExpression::class.java)
        Assertions.assertThat(and(Any::class.java)).isInstanceOf(
            TokenTypeClassExpression::class.java
        )
        Assertions.assertThat(GrammarFunctions.Standard.firstOf(e1)).isSameAs(e1)
        Assertions.assertThat(GrammarFunctions.Standard.firstOf(e1, e2)).isInstanceOf(
            FirstOfExpression::class.java
        )
        Assertions.assertThat(or(e1)).isSameAs(e1)
        Assertions.assertThat(or(e1, e2)).isInstanceOf(FirstOfExpression::class.java)
        Assertions.assertThat(opt(e1)).isInstanceOf(OptionalExpression::class.java)
        Assertions.assertThat(o2n(e1)).isInstanceOf(ZeroOrMoreExpression::class.java)
        Assertions.assertThat(one2n(e1)).isInstanceOf(OneOrMoreExpression::class.java)
        Assertions.assertThat(GrammarFunctions.Predicate.next(e1)).isInstanceOf(NextExpression::class.java)
        Assertions.assertThat(not(e1)).isInstanceOf(NextNotExpression::class.java)
        Assertions.assertThat(isTrue()).`as`("singleton").isSameAs(AnyTokenExpression.INSTANCE)
        Assertions.assertThat(isFalse()).`as`("singleton").isSameAs(NothingExpression.INSTANCE)
        Assertions.assertThat(tillNewLine()).`as`("singleton").isSameAs(TillNewLineExpression.INSTANCE)
        Assertions.assertThat(
            bridge(
                Mockito.mock(
                    TokenType::class.java
                ), Mockito.mock(TokenType::class.java)
            )
        ).isInstanceOf(
            TokensBridgeExpression::class.java
        )
        Assertions.assertThat(
            isOneOfThem(
                Mockito.mock(
                    TokenType::class.java
                ), Mockito.mock(TokenType::class.java)
            )
        ).isInstanceOf(
            TokenTypesExpression::class.java
        )
        Assertions.assertThat(adjacent(e1).toString()).isEqualTo("Sequence[Adjacent, $e1]")
        Assertions.assertThat(anyTokenButNot(e1).toString()).isEqualTo("Sequence[NextNot[$e1], AnyToken]")
        Assertions.assertThat(till(e1).toString())
            .isEqualTo("Sequence[ZeroOrMore[Sequence[NextNot[$e1], AnyToken]], $e1]")
        Assertions.assertThat(exclusiveTill(e1).toString()).isEqualTo("ZeroOrMore[Sequence[NextNot[$e1], AnyToken]]")
        Assertions.assertThat(exclusiveTill(e1, e2).toString()).isEqualTo(
            "ZeroOrMore[Sequence[NextNot[FirstOf[$e1, $e2]], AnyToken]]"
        )
    }

    @Test
    fun firstOf_requires_at_least_one_argument() {
        assertThrows("You must define at least one matcher.", IllegalArgumentException::class.java) {
            GrammarFunctions.Standard.firstOf()
        }
    }

    @Test
    fun and_requires_at_least_one_argument() {
        assertThrows("You must define at least one matcher.", IllegalArgumentException::class.java) {
            and()
        }
    }

    @Test
    fun isOneOfThem_requires_at_least_one_argument() {
        assertThrows("You must define at least one matcher.", IllegalArgumentException::class.java) {
            isOneOfThem()
        }
    }

    @Test
    fun test_incorrect_type_of_parsing_expression() {
        assertThrows("java.lang.Object", IllegalArgumentException::class.java) {
            and(Any())
        }
    }

    @Test
    @Throws(Exception::class)
    fun private_constructors() {
        Assertions.assertThat(hasPrivateConstructor(GrammarFunctions::class.java)).isTrue()
        Assertions.assertThat(hasPrivateConstructor(GrammarFunctions.Standard::class.java)).isTrue()
        Assertions.assertThat(hasPrivateConstructor(GrammarFunctions.Predicate::class.java)).isTrue()
        Assertions.assertThat(hasPrivateConstructor(Advanced::class.java)).isTrue()
    }

    companion object {
        @Throws(Exception::class)
        private fun hasPrivateConstructor(cls: Class<*>): Boolean {
            val constructor = cls.getDeclaredConstructor()
            val result = !constructor.isAccessible
            constructor.isAccessible = true
            constructor.newInstance()
            return result
        }
    }
}