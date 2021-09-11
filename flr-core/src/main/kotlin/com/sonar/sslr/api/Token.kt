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
package com.sonar.sslr.api

import java.net.URI
import java.util.*

/**
 * Tokens are string of character like an identifier, a literal, an integer, ... which are produced by the lexer to feed the parser.
 * By definition, comments and preprocessing directives should not be seen by the parser that's why such Trivia, when they exist, are
 * attached to the next token.
 */
public class Token private constructor(builder: Builder) {
    public val type: TokenType
    public val value: String

    /**
     * @return the original value of the token. This method is useful when a language is case-insensitive as in that case all token values are
     * capitalized.
     */
    public val originalValue: String

    /**
     * @return the line of the token in the source code
     */
    public val line: Int

    /**
     * @return the column of the token in the source code
     */
    public val column: Int

    /**
     * @return the URI this token belongs to
     */
    public val uRI: URI?
    public val isGeneratedCode: Boolean

    /**
     * @return the list of trivia located between this token and the previous one
     */
    public val trivia: List<Trivia>
    public val isCopyBook: Boolean
    public val copyBookOriginalLine: Int
    public val copyBookOriginalFileName: String

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
        internal var uri: URI? = null
        internal var line = 0
        internal var column = -1
        internal var trivia = mutableListOf<Trivia>()
        internal var generatedCode = false
        internal var copyBook = false
        internal var copyBookOriginalLine = -1
        internal var copyBookOriginalFileName = ""

        public constructor()
        public constructor(token: Token) {
            type = token.type
            value = token.value
            originalValue = token.originalValue
            uri = token.uRI
            line = token.line
            column = token.column
            trivia.addAll(token.trivia)
            generatedCode = token.isGeneratedCode
            copyBook = token.isCopyBook
            copyBookOriginalLine = token.copyBookOriginalLine
            copyBookOriginalFileName = token.copyBookOriginalFileName
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
            Objects.requireNonNull(value, "value cannot be null")
            Objects.requireNonNull(originalValue, "originalValue cannot be null")
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

        public fun setURI(uri: URI?): Builder {
            Objects.requireNonNull(uri, "uri cannot be null")
            this.uri = uri
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

        /**
         * @since 1.17
         */
        public fun notCopyBook(): Builder {
            copyBook = false
            copyBookOriginalLine = -1
            copyBookOriginalFileName = ""
            return this
        }

        public fun setCopyBook(copyBookOriginalFileName: String, copyBookOriginalLine: Int): Builder {
            Objects.requireNonNull(copyBookOriginalFileName, "copyBookOriginalFileName cannot be null")
            copyBook = true
            this.copyBookOriginalFileName = copyBookOriginalFileName
            this.copyBookOriginalLine = copyBookOriginalLine
            return this
        }

        public fun build(): Token {
            Objects.requireNonNull(type, "type must be set")
            Objects.requireNonNull(value, "value must be set")
            Objects.requireNonNull(originalValue, "originalValue must be set")
            Objects.requireNonNull(uri, "file must be set")
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
    }

    init {
        type = builder.type
        value = builder.value
        originalValue = builder.originalValue
        line = builder.line
        column = builder.column
        uRI = builder.uri
        isGeneratedCode = builder.generatedCode
        trivia = if (builder.trivia.isEmpty()) emptyList() else ArrayList(builder.trivia)
        isCopyBook = builder.copyBook
        copyBookOriginalLine = builder.copyBookOriginalLine
        copyBookOriginalFileName = builder.copyBookOriginalFileName
    }
}