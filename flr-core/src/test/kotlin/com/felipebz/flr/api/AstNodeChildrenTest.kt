/**
 * FLR
 * Copyright (C) 2021-2026 Felipe Zorzo
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
package com.felipebz.flr.api

import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.grammar.LexerfulGrammarBuilder
import com.felipebz.flr.impl.Lexer
import com.felipebz.flr.impl.Parser
import com.felipebz.flr.impl.channel.BlackHoleChannel
import com.felipebz.flr.impl.channel.IdentifierAndKeywordChannel
import com.felipebz.flr.impl.channel.RegexpChannel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AstNodeChildrenTest {
    private enum class Rules : GrammarRuleKey { ROOT, WRAP, LIST, TAIL, PAIR, WORD, ITEM }

    private object Kept : AstNodeType

    private object Skipped : AstNodeType, AstNodeSkippingPolicy {
        override fun hasToBeSkippedFromAst(node: AstNode): Boolean = true
    }

    /** `x` is a kept leaf named x, `skip:a,b` a node to be skipped holding the leaves a and b, null a null candidate. */
    private fun candidates(vararg spec: String?): List<AstNode?> = spec.map { item ->
        when {
            item == null -> null
            item.startsWith("skip:") -> AstNode(Skipped, "skip", null).also { skipped ->
                item.removePrefix("skip:").split(",").filter { it.isNotEmpty() }
                    .forEach { skipped.addChild(AstNode(Kept, it, null)) }
            }
            else -> AstNode(Kept, item, null)
        }
    }

    private fun parse(source: String): AstNode {
        val b = LexerfulGrammarBuilder.create()
        b.rule(Rules.WORD).`is`(GenericTokenType.IDENTIFIER)
        b.rule(Rules.PAIR).`is`(Rules.WORD, Rules.WORD)
        b.rule(Rules.TAIL).`is`(",", Rules.WORD).skip()
        b.rule(Rules.LIST).`is`("(", Rules.WORD, b.zeroOrMore(Rules.TAIL), ")")
        b.rule(Rules.WRAP).`is`(Rules.LIST)
        b.rule(Rules.ITEM).`is`(Rules.WORD, b.optional("!")).skipIfOneChild()
        b.rule(Rules.ROOT).`is`(Rules.WRAP, Rules.PAIR, b.oneOrMore(Rules.ITEM), GenericTokenType.EOF)
        b.setRootRule(Rules.ROOT)
        val lexer = Lexer.builder()
            .withFailIfNoChannelToConsumeOneCharacter(true)
            .withChannel(IdentifierAndKeywordChannel("[a-z]++", true))
            .withChannel(RegexpChannel(GenericTokenType.LITERAL, "[(),!]"))
            .withChannel(BlackHoleChannel("[ \\t\\r\\n]++"))
            .build()
        return Parser.builder(b.build()).withLexer(lexer).build().parse(source)
    }

    /** Nodes with children as `NAME[children]`, the others as their token value. */
    private fun AstNode.dump(): String =
        if (hasChildren()) "$name[${children.joinToString(" ") { it.dump() }}]" else tokenValue

    /** Every child of [node], at any depth, has [node] as parent and the siblings its position in the list gives. */
    private fun assertLinks(node: AstNode) {
        val children = node.children
        assertThat(node.numberOfChildren).isEqualTo(children.size)
        assertThat(node.hasChildren()).isEqualTo(children.isNotEmpty())
        for ((index, child) in children.withIndex()) {
            assertThat(child.parent).isSameAs(node)
            assertThat(child.previousSiblingOrNull).isSameAs(children.getOrNull(index - 1))
            assertThat(child.nextSiblingOrNull).isSameAs(children.getOrNull(index + 1))
            assertLinks(child)
        }
    }

    @Test
    fun childrenAndSiblingsOfTerminalsOneChildAndManyChildNodes() {
        val root = parse("(a, b, c) x y z w!")

        // TAIL is skipped into LIST; the ITEM with one child is replaced by its WORD.
        assertThat(root.dump()).isEqualTo(
            "ROOT[WRAP[LIST[( WORD[a] , WORD[b] , WORD[c] )]] PAIR[WORD[x] WORD[y]] WORD[z] ITEM[WORD[w] !] EOF]"
        )
        assertLinks(root)

        val wrap = root.firstChild
        val list = wrap.firstChild
        assertThat(wrap.lastChild).isSameAs(list)
        assertThat(list.firstChild.tokenValue).isEqualTo("(")
        assertThat(list.lastChild.tokenValue).isEqualTo(")")
        val terminal = list.children[1].firstChild
        assertThat(terminal.hasChildren()).isFalse()
        assertThat(terminal.firstChildOrNull).isNull()
        assertThat(terminal.lastChildOrNull).isNull()
        assertThat(list.children[1].nextSibling.nextSibling.firstChild.tokenValue).isEqualTo("b")
        assertThat(root.lastChild.type).isSameAs(GenericTokenType.EOF)
        assertThat(root.lastChild.previousSibling.name).isEqualTo("ITEM")
    }

    @Test
    fun addChildAppendsToNodesOfAParsedTree() {
        val root = parse("(a) x y z")
        val pair = root.children[1]
        val word = pair.firstChild
        val toMany = AstNode(Rules.WORD, "WORD", null)
        val toOne = AstNode(Rules.WORD, "WORD", null)
        val toNone = AstNode(Rules.WORD, "WORD", null)

        pair.addChild(toMany)
        word.addChild(toOne)
        word.firstChild.addChild(toNone)

        assertThat(pair.children).hasSize(3)
        assertThat(pair.lastChild).isSameAs(toMany)
        assertThat(toMany.previousSibling).isSameAs(pair.children[1])
        assertThat(word.children).hasSize(2)
        assertThat(toOne.previousSibling.tokenValue).isEqualTo("x")
        assertThat(word.firstChild.children).containsExactly(toNone)
        assertLinks(root)
    }

    @Test
    fun setChildrenFlattensSkippedCandidatesAsAddChildDoes() {
        val several = arrayOf("a", null, "skip:", "skip:b", "skip:c,d,e", "f")
        val cases = listOf(
            Triple(several, 0 to several.size, listOf("a", "b", "c", "d", "e", "f")),
            Triple(several, 1 to 5, listOf("b", "c", "d", "e")),
            Triple(arrayOf(null, "skip:", "skip:x", null), 0 to 4, listOf("x")),
            Triple(arrayOf("skip:", "y"), 0 to 2, listOf("y")),
            Triple(arrayOf(null, "skip:"), 0 to 2, emptyList())
        )
        for ((spec, range, expected) in cases) {
            val (from, to) = range
            val viaSet = AstNode(Kept, "parent", null)
            viaSet.setChildren(candidates(*spec), from, to)
            val viaAdd = AstNode(Kept, "parent", null)
            candidates(*spec).subList(from, to).forEach(viaAdd::addChild)

            assertThat(viaSet.children.map { it.name }).describedAs(spec.contentToString()).isEqualTo(expected)
            assertThat(viaAdd.children.map { it.name }).isEqualTo(expected)
            assertLinks(viaSet)
        }
    }
}
