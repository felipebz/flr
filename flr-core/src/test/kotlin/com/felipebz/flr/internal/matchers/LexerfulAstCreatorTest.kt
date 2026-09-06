/**
 * FLR
 * Copyright (C) 2021-2026 Felipe Zorzo
 * mailto:felipe AT felipezorzo DOT com DOT br
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 */
package com.felipebz.flr.internal.matchers

import com.felipebz.flr.api.AstNode
import com.felipebz.flr.api.GenericTokenType
import com.felipebz.flr.api.Token
import com.felipebz.flr.impl.matcher.RuleDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LexerfulAstCreatorTest {
    @Test
    fun projectsAlwaysSkipChildrenIntoTheParent() {
        val tokens = List(6) { token(it) }
        val root = RuleDefinition("root")
        val alwaysOuter = RuleDefinition("alwaysOuter").also { it.skip() }
        val alwaysInner = RuleDefinition("alwaysInner").also { it.skip() }
        val retained = RuleDefinition("retained")
        val oneChild = RuleDefinition("oneChild").also { it.skipIfOneChild() }
        val multipleChildren = RuleDefinition("multipleChildren")
        val emptyAlways = RuleDefinition("emptyAlways").also { it.skip() }

        val parseTree = ParseNode(
            0,
            6,
            root,
            arrayOf(
                ParseNode(
                    0,
                    3,
                    alwaysOuter,
                    arrayOf(
                        terminal(0),
                        ParseNode(1, 2, alwaysInner, arrayOf(terminal(1))),
                        ParseNode(2, 3, retained, arrayOf(terminal(2)))
                    )
                ),
                ParseNode(3, 4, oneChild, arrayOf(terminal(3))),
                ParseNode(4, 6, multipleChildren, arrayOf(terminal(4), terminal(5))),
                ParseNode(6, 6, emptyAlways)
            )
        )

        val ast = create(parseTree, tokens)

        assertThat(ast.name).isEqualTo("root")
        assertThat(ast.fromIndex).isEqualTo(0)
        assertThat(ast.toIndex).isEqualTo(6)
        assertThat(ast.children).hasSize(5)
        assertThat(ast.children[0].tokenOriginalValue).isEqualTo("token0")
        assertThat(ast.children[1].tokenOriginalValue).isEqualTo("token1")
        assertThat(ast.children[2].name).isEqualTo("retained")
        assertThat(ast.children[2].parent).isSameAs(ast)
        assertThat(ast.children[2].children[0].tokenOriginalValue).isEqualTo("token2")
        assertThat(ast.children[3].tokenOriginalValue).isEqualTo("token3")
        assertThat(ast.children[4].name).isEqualTo("multipleChildren")
        assertThat(ast.children[4].children).extracting<String> { it.tokenOriginalValue }
            .containsExactly("token4", "token5")
        assertThat(ast.children[0].parent).isSameAs(ast)
        assertThat(ast.children[1].parent).isSameAs(ast)
        assertThat(ast.children[3].parent).isSameAs(ast)
    }

    @Test
    fun preservesAnAlwaysSkipRoot() {
        val root = RuleDefinition("root").also { it.skip() }
        val ast = create(ParseNode(0, 1, root, arrayOf(terminal(0))), listOf(token(0)))

        assertThat(ast.name).isEqualTo("root")
        assertThat(ast.children).hasSize(1)
        assertThat(ast.children[0].parent).isSameAs(ast)
    }

    private fun create(parseTree: ParseNode, tokens: List<Token>): AstNode {
        return LexerfulAstCreator.create(
            parseTree,
            tokens,
            { type, name, token -> AstNode(type, name, token) },
            { token -> AstNode(token) }
        )
    }

    private fun terminal(index: Int): ParseNode {
        return ParseNode(index, index + 1, null)
    }

    private fun token(index: Int): Token {
        return Token.builder()
            .setType(GenericTokenType.IDENTIFIER)
            .setValueAndOriginalValue("token$index")
            .setLine(1)
            .setColumn(index)
            .build()
    }
}
