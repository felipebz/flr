/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.examples.grammars

import com.felipebz.flr.examples.grammars.IncorrectGrammar.Companion.infiniteOneOrMore
import com.felipebz.flr.examples.grammars.IncorrectGrammar.Companion.infiniteZeroOrMore
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.parser.ParseRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.regex.PatternSyntaxException

class IncorrectGrammarTest {
    @Test
    fun undefined_rule() {
        assertThrows<GrammarException>("The rule 'A' hasn't been defined.") {
            IncorrectGrammar.undefinedRule()
        }
    }

    @Test
    fun reference_to_undefined_rule() {
        assertThrows<GrammarException>("The rule 'B' hasn't been defined.") {
            IncorrectGrammar.referenceToUndefinedRule()
        }
    }

    @Test
    fun rule_defined_twice() {
        assertThrows<GrammarException>("The rule 'A' has already been defined somewhere in the grammar.") {
            IncorrectGrammar.ruleDefinedTwice()
        }
    }

    @Test
    fun incorrect_regular_expression() {
        assertThrows<PatternSyntaxException>("Dangling meta character '*' near index 0") {
            IncorrectGrammar.incorrectRegularExpression()
        }
    }

    @Test
    fun infinite_zero_or_more_expression() {
        assertThrows<GrammarException>("The inner part of ZeroOrMore and OneOrMore must not allow empty matches") {
            ParseRunner(infiniteZeroOrMore().rule(IncorrectGrammar.A)).parse("foo".toCharArray())
        }
    }

    @Test
    fun infinite_one_or_more_expression() {
        assertThrows<GrammarException>("The inner part of ZeroOrMore and OneOrMore must not allow empty matches") {
            ParseRunner(infiniteOneOrMore().rule(IncorrectGrammar.A)).parse("foo".toCharArray())
        }
    }
}
