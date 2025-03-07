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
package com.felipebz.flr.api.typed

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.AstNodeType
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.RecognitionException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerlessGrammarBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.NoSuchFileException
import java.util.*

class ActionParserTest {
    @Test
    fun not_matching() {
        assertThrows<RecognitionException> {
            parse(MyGrammarKeys.NUMERIC, "x")
        }
    }

    @Test
    fun basic() {
        assertThat(parse(MyGrammarKeys.NUMERIC, "42", Numeric::class.java).toString()).isEqualTo("42")
    }

    @Test
    fun firstOf() {
        assertThat(parse(MyGrammarKeys.OPERATOR, "+", Operator::class.java).toString()).isEqualTo("+")
        assertThat(parse(MyGrammarKeys.OPERATOR, "-", Operator::class.java).toString()).isEqualTo("-")
        assertNotParse(MyGrammarKeys.OPERATOR, "x")
    }

    @Test
    fun optional() {
        assertThat(parse(MyGrammarKeys.UNARY_EXP, "42", UnaryExp::class.java).toString()).isEqualTo("42")
        assertThat(parse(MyGrammarKeys.UNARY_EXP, "+42", UnaryExp::class.java).toString()).isEqualTo("+ 42")
    }

    @Test
    fun oneOrMore() {
        assertThat(parse(MyGrammarKeys.NUMERIC_LIST, "42 7", NumericList::class.java).toString())
            .isEqualTo("[42, 7]")
        assertNotParse(MyGrammarKeys.NUMERIC_LIST, "")
    }

    @Test
    fun zeroOrMore() {
        assertThat(
            parse(
                MyGrammarKeys.POTENTIALLY_EMPTY_NUMERIC_LIST,
                "42 7",
                NumericList::class.java
            ).toString()
        ).isEqualTo("[42, 7]")
        assertThat(
            parse(
                MyGrammarKeys.POTENTIALLY_EMPTY_NUMERIC_LIST,
                "",
                NumericList::class.java
            ).toString()
        ).isEqualTo("[]")
    }

    @Test
    fun skipped_astnode() {
        assertThat(parse(MyGrammarKeys.NUMERIC_WITH_EOF, "42", Numeric::class.java).toString())
            .isEqualTo("42")
    }

    @Test
    fun undefined_token_type() {
        val numeric = parse(MyGrammarKeys.NUMERIC, "42", Numeric::class.java)
        val firstChild = checkNotNull(numeric.firstChild)
        val type = firstChild.token.type
        assertThat(type.hasToBeSkippedFromAst(null)).isFalse()
        assertThat(type.value).isEqualTo("TOKEN")
        assertThat(type.toString()).isNotNull
    }

    @Test
    fun comment() {
        val numeric = parse(MyGrammarKeys.NUMERIC, "/* myComment */42", Numeric::class.java)
        val firstChild = checkNotNull(numeric.firstChild)
        val trivia = firstChild.token.trivia[0]
        assertThat(trivia.isComment).isTrue()
        assertThat(trivia.token.originalValue).isEqualTo("/* myComment */")
    }

    @Test
    fun skipped_text() {
        assertThat(parse(MyGrammarKeys.NUMERIC, "  42", Numeric::class.java).toString()).isEqualTo("42")
    }

    @Test
    fun rootRule() {
        assertThat(parser(MyGrammarKeys.OPERATOR).rootRule()).isEqualTo(MyGrammarKeys.OPERATOR)
    }

    @Test
    fun parse_file() {
        val parser = parser(MyGrammarKeys.UNARY_EXP)
        val node = parser.parse(File("src/test/resources/typed/42.txt"))
        assertThat(node).isInstanceOf(UnaryExp::class.java)
    }

    @Test
    fun unknown_file() {
        val parser = parser(MyGrammarKeys.NUMERIC)
        try {
            parser.parse(File("unknown"))
            fail("expceted exception")
        } catch (e: RuntimeException) {
            assertThat(e.cause).isInstanceOf(NoSuchFileException::class.java)
        }
    }

    @Test
    fun more_than_one_call_to_the_same_action_method() {
        assertThat(parse(MyGrammarKeys.NUMERIC, "42", Numeric::class.java).toString()).isEqualTo("42")
        assertThat(parse(MyGrammarKeys.NUMERIC2, "42", Numeric::class.java).toString()).isEqualTo("42")
    }

    private fun <T : AstNode> parse(ruleKey: GrammarRuleKey, toParse: String, expectedClass: Class<T>): T {
        val astNode = parse(ruleKey, toParse)
        assertThat(astNode).isInstanceOf(expectedClass)
        return astNode as T
    }

    private fun parse(ruleKey: GrammarRuleKey, toParse: String): AstNode {
        return checkNotNull(parser(ruleKey).parse(toParse))
    }

    private fun parser(ruleKey: GrammarRuleKey): ActionParser<AstNode> {
        val b = LexerlessGrammarBuilder.create()
        b.rule(MyGrammarKeys.PLUS).`is`(b.regexp("\\+"))
        b.rule(MyGrammarKeys.MINUS).`is`(b.regexp("-"))
        b.rule(MyGrammarKeys.NUMERIC_TOKEN).`is`(
            b.optional(b.commentTrivia(b.regexp("/\\*[\\s\\S]*?\\*/"))),
            b.optional(b.skippedTrivia(b.regexp("\\s+"))),
            b.regexp("[0-9]+")
        )
        b.rule(MyGrammarKeys.EOF).`is`(b.token(GenericTokenType.EOF, b.endOfInput())).skip()
        return ActionParser(
            StandardCharsets.UTF_8,
            b,
            MyGrammar::class.java,
            MyTreeFactory(),
            AstNodeBuilder(),
            ruleKey
        )
    }

    private fun assertNotParse(ruleKey: GrammarRuleKey, toParse: String) {
        try {
            parse(ruleKey, toParse)
            fail("$ruleKey should not match '$toParse'")
        } catch (e: RecognitionException) {
            // OK
        }
    }

    private enum class MyGrammarKeys : GrammarRuleKey, AstNodeType {
        NUMERIC, NUMERIC2, NUMERIC_TOKEN, PLUS, MINUS, OPERATOR, UNARY_EXP, NUMERIC_LIST, POTENTIALLY_EMPTY_NUMERIC_LIST, EOF, NUMERIC_WITH_EOF
    }

    open class MyGrammar(private val b: GrammarBuilder<AstNode>, private val f: MyTreeFactory) {
        open fun NUMERIC(): Numeric {
            return b.nonterminal<Numeric>(MyGrammarKeys.NUMERIC)
                .`is`(f.numeric(b.invokeRule(MyGrammarKeys.NUMERIC_TOKEN)))
        }

        // Includes a 2nd call to MyTreeFactory.numeric(...)
        open fun NUMERIC2(): Numeric {
            return b.nonterminal<Numeric>(MyGrammarKeys.NUMERIC2)
                .`is`(f.numeric(b.invokeRule(MyGrammarKeys.NUMERIC_TOKEN)))
        }

        open fun OPERATOR(): Operator {
            return b.nonterminal<Operator>(MyGrammarKeys.OPERATOR)
                .`is`(
                    f.operator(
                        b.firstOf(
                            b.token(MyGrammarKeys.PLUS),
                            b.token(MyGrammarKeys.MINUS)
                        )
                    )
                )
        }

        open fun UNARY_EXP(): UnaryExp {
            return b.nonterminal<UnaryExp>(MyGrammarKeys.UNARY_EXP)
                .`is`(
                    f.unaryExp(
                        b.optional(b.token(MyGrammarKeys.PLUS)),
                        NUMERIC()
                    )
                )
        }

        open fun NUMERIC_LIST(): AstNode {
            return b.nonterminal<AstNode>(MyGrammarKeys.NUMERIC_LIST)
                .`is`(
                    f.numericList(b.oneOrMore(NUMERIC()))
                )
        }

        open fun POTENTIALLY_EMPTY_EXP_LIST(): AstNode {
            return b.nonterminal<AstNode>(MyGrammarKeys.POTENTIALLY_EMPTY_NUMERIC_LIST)
                .`is`(
                    f.numericList(b.zeroOrMore(NUMERIC()))
                )
        }

        open fun NUMERIC_WITH_EOF(): AstNode {
            return b.nonterminal<Numeric>(MyGrammarKeys.NUMERIC_WITH_EOF)
                .`is`(
                    f.numeric(
                        NUMERIC(),
                        b.invokeRule(MyGrammarKeys.EOF)
                    )
                )
        }
    }

    open class MyTree(type: AstNodeType) : AstNode(type, type.toString(), null)
    class Numeric(private val node: AstNode?) : MyTree(MyGrammarKeys.NUMERIC) {
        override fun toString(): String {
            return node?.tokenValue ?: ""
        }

        init {
            addChild(node)
        }
    }

    class Operator(node: AstNode?) : MyTree(MyGrammarKeys.OPERATOR) {
        private val value: String? = node?.tokenValue

        override fun toString(): String {
            return value.orEmpty()
        }
    }

    class UnaryExp(private val plus: Optional<AstNode>?, private val operand: Numeric) :
        MyTree(MyGrammarKeys.UNARY_EXP) {
        override fun toString(): String {
            return (if (checkNotNull(plus).isPresent) "+ " else "") + operand
        }
    }

    class NumericList(private val list: List<Numeric>?) : MyTree(MyGrammarKeys.NUMERIC_LIST) {
        override fun toString(): String {
            return list.toString()
        }
    }

    open class MyTreeFactory {
        open fun unaryExp(plus: Optional<AstNode>?, numeric: Numeric): UnaryExp {
            return UnaryExp(plus, numeric)
        }

        open fun numericList(list: List<Numeric>?): AstNode {
            return NumericList(list)
        }

        open fun numericList(list: Optional<List<Numeric>>?): AstNode {
            return NumericList(if (list?.isPresent == true) list.get() else listOf())
        }

        open fun operator(node: AstNode?): Operator {
            return Operator(node)
        }

        open fun numeric(node: AstNode?): Numeric {
            return Numeric(node)
        }

        open fun numeric(numeric: Numeric, eof: AstNode?): Numeric {
            return numeric
        }
    }
}
