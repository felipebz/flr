/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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

import java.util.*

/**
 * Tokens are string of character like an identifier, a literal, an integer, ... which are produced by the lexer to feed the parser.
 * By definition, comments and preprocessing directives should not be seen by the parser that's why such Trivia, when they exist, are
 * attached to the next token.
 */
public class Token private constructor(builder: Builder) {
    public val type: TokenType = builder.type
    public val value: String = builder.value

    /**
     * @return the original value of the token. This method is useful when a language is case-insensitive as in that case all token values are
     * capitalized.
     */
    public val originalValue: String = builder.originalValue

    /**
     * @return the line of the token in the source code
     */
    public val line: Int = builder.line

    /**
     * @return the column of the token in the source code
     */
    public val column: Int = builder.column

    public val endLine: Int

    public val endColumn: Int

    public val isGeneratedCode: Boolean = builder.generatedCode

    /**
     * @return the list of trivia located between this token and the previous one
     */
    public val trivia: List<Trivia> = if (builder.trivia.isEmpty()) emptyList() else ArrayList(builder.trivia)

    init {
        var lastLineLength = 0
        var lineCount = 1

        if (value.indexOf('\n') != -1 || value.indexOf('\r') != -1) {
            val lines = pattern.split(value)
            lineCount = if (lines.size > 1) {
                lastLineLength = lines[lines.size - 1].length
                lines.size
            } else {
                1
            }
        }

        endLine = line + lineCount - 1
        val endLineOffset = if (endLine != line) {
            lastLineLength
        } else {
            column + value.length
        }

        endColumn = endLineOffset
    }

    /**
     * @return true if there is some trivia like some comments or preprocessing directive between this token and the previous one.
     */
    public fun hasTrivia(): Boolean {
        return trivia.isNotEmpty()
    }

    public fun isOnSameLineThan(other: Token?): Boolean {
        return if (other == null) false else line == other.line
    }

    override fun toString(): String {
        return "$type: $value"
    }

    /**
     * Instances can be reused - it is safe to call [.build]
     * multiple times to build multiple tokens in series.
     */
    public class Builder {
        internal lateinit var type: TokenType
        internal var value: String = ""
        internal var originalValue: String = ""
        internal var line = 0
        internal var column = -1
        internal var trivia = mutableListOf<Trivia>()
        internal var generatedCode = false

        public constructor()
        public constructor(token: Token) {
            type = token.type
            value = token.value
            originalValue = token.originalValue
            line = token.line
            column = token.column
            trivia.addAll(token.trivia)
            generatedCode = token.isGeneratedCode
        }

        public fun setType(type: TokenType): Builder {
            Objects.requireNonNull(type, "type cannot be null")
            this.type = type
            return this
        }

        public fun setValueAndOriginalValue(valueAndOriginalValue: String): Builder {
            value = valueAndOriginalValue
            originalValue = valueAndOriginalValue
            return this
        }

        public fun setValueAndOriginalValue(value: String, originalValue: String): Builder {
            this.value = value
            this.originalValue = originalValue
            return this
        }

        public fun setLine(line: Int): Builder {
            this.line = line
            return this
        }

        public fun setColumn(column: Int): Builder {
            this.column = column
            return this
        }

        public fun setGeneratedCode(generatedCode: Boolean): Builder {
            this.generatedCode = generatedCode
            return this
        }

        public fun setTrivia(trivia: List<Trivia>): Builder {
            this.trivia = trivia.toMutableList()
            return this
        }

        public fun addTrivia(trivia: Trivia): Builder {
            if (this.trivia.isEmpty()) {
                this.trivia = mutableListOf()
            }
            this.trivia.add(trivia)
            return this
        }

        public fun build(): Token {
            require(line >= 1) { "line must be greater or equal than 1" }
            require(column >= 0) { "column must be greater or equal than 0" }
            return Token(this)
        }
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder {
            return Builder()
        }

        @JvmStatic
        public fun builder(token: Token): Builder {
            return Builder(token)
        }

        private val pattern = Regex("\\R")
    }
}