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
import org.fest.assertions.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.mock
import org.sonar.sslr.internal.vm.*
import org.sonar.sslr.internal.vm.lexerful.*

class GrammarFunctionsTest {
    @Test
    fun test() {
        val e1 = mock<ParsingExpression>()
        val e2 = mock<ParsingExpression>()
        val rule = mock<RuleDefinition>()
        assertThat(and(rule)).isSameAs(rule)
        assertThat(and(e1)).isSameAs(e1)
        assertThat(and(e1, e2)).isInstanceOf(SequenceExpression::class.java)
        assertThat(and("foo")).isInstanceOf(
            TokenValueExpression::class.java
        )
        assertThat(and(mock<TokenType>())).isInstanceOf(TokenTypeExpression::class.java)
        assertThat(and(Any::class.java)).isInstanceOf(
            TokenTypeClassExpression::class.java
        )
        assertThat(GrammarFunctions.Standard.firstOf(e1)).isSameAs(e1)
        assertThat(GrammarFunctions.Standard.firstOf(e1, e2)).isInstanceOf(
            FirstOfExpression::class.java
        )
        assertThat(or(e1)).isSameAs(e1)
        assertThat(or(e1, e2)).isInstanceOf(FirstOfExpression::class.java)
        assertThat(opt(e1)).isInstanceOf(OptionalExpression::class.java)
        assertThat(o2n(e1)).isInstanceOf(ZeroOrMoreExpression::class.java)
        assertThat(one2n(e1)).isInstanceOf(OneOrMoreExpression::class.java)
        assertThat(GrammarFunctions.Predicate.next(e1)).isInstanceOf(NextExpression::class.java)
        assertThat(not(e1)).isInstanceOf(NextNotExpression::class.java)
        assertThat(isTrue()).`as`("singleton").isSameAs(AnyTokenExpression.INSTANCE)
        assertThat(isFalse()).`as`("singleton").isSameAs(NothingExpression.INSTANCE)
        assertThat(tillNewLine()).`as`("singleton").isSameAs(TillNewLineExpression.INSTANCE)
        assertThat(bridge(mock(), mock())).isInstanceOf(TokensBridgeExpression::class.java)
        assertThat(isOneOfThem(mock(), mock())).isInstanceOf(TokenTypesExpression::class.java)
        assertThat(adjacent(e1).toString()).isEqualTo("Sequence[Adjacent, $e1]")
        assertThat(anyTokenButNot(e1).toString()).isEqualTo("Sequence[NextNot[$e1], AnyToken]")
        assertThat(till(e1).toString())
            .isEqualTo("Sequence[ZeroOrMore[Sequence[NextNot[$e1], AnyToken]], $e1]")
        assertThat(exclusiveTill(e1).toString()).isEqualTo("ZeroOrMore[Sequence[NextNot[$e1], AnyToken]]")
        assertThat(exclusiveTill(e1, e2).toString()).isEqualTo(
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
        assertThat(hasPrivateConstructor(GrammarFunctions::class.java)).isTrue()
        assertThat(hasPrivateConstructor(GrammarFunctions.Standard::class.java)).isTrue()
        assertThat(hasPrivateConstructor(GrammarFunctions.Predicate::class.java)).isTrue()
        assertThat(hasPrivateConstructor(Advanced::class.java)).isTrue()
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