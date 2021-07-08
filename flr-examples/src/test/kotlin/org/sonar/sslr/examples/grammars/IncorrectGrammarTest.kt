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
package org.sonar.sslr.examples.grammars

import org.junit.Assert.assertThrows
import org.junit.Test
import org.sonar.sslr.examples.grammars.IncorrectGrammar.Companion.infiniteOneOrMore
import org.sonar.sslr.examples.grammars.IncorrectGrammar.Companion.infiniteZeroOrMore
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.parser.ParseRunner
import java.util.regex.PatternSyntaxException

class IncorrectGrammarTest {
    @Test
    fun undefined_rule() {
        assertThrows("The rule 'A' hasn't been defined.", GrammarException::class.java) {
            IncorrectGrammar.undefinedRule()
        }
    }

    @Test
    fun reference_to_undefined_rule() {
        assertThrows("The rule 'B' hasn't been defined.", GrammarException::class.java) {
            IncorrectGrammar.referenceToUndefinedRule()
        }
    }

    @Test
    fun rule_defined_twice() {
        assertThrows("The rule 'A' has already been defined somewhere in the grammar.", GrammarException::class.java) {
            IncorrectGrammar.ruleDefinedTwice()
        }
    }

    @Test
    fun incorrect_regular_expression() {
        assertThrows("Dangling meta character '*' near index 0", PatternSyntaxException::class.java) {
            IncorrectGrammar.incorrectRegularExpression()
        }
    }

    @Test
    fun infinite_zero_or_more_expression() {
        assertThrows("The inner part of ZeroOrMore and OneOrMore must not allow empty matches", GrammarException::class.java) {
            ParseRunner(infiniteZeroOrMore().rule(IncorrectGrammar.A)).parse("foo".toCharArray())
        }
    }

    @Test
    fun infinite_one_or_more_expression() {
        assertThrows("The inner part of ZeroOrMore and OneOrMore must not allow empty matches", GrammarException::class.java) {
            ParseRunner(infiniteOneOrMore().rule(IncorrectGrammar.A)).parse("foo".toCharArray())
        }
    }
}