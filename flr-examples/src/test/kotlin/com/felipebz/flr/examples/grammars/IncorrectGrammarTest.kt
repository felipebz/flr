//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
