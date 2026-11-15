//
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.tests

import com.felipebz.flr.api.Token
import com.felipebz.flr.api.TokenType
import org.assertj.core.api.AbstractAssert

public class TokenListAssert(actual: List<Token>?) : AbstractAssert<TokenListAssert, List<Token>>(
    actual, TokenListAssert::class.java
) {

    public fun doesNotHaveToken(tokenValue: String, tokenType: TokenType): TokenListAssert {
        isNotNull
        for (token in actual) {
            if (token.value == tokenValue && token.type === tokenType) {
                throw failure("Expected to have token '$tokenValue' of type $tokenType but was not found")
            }
        }
        return this
    }

    public fun hasToken(tokenValue: String, tokenType: TokenType): TokenListAssert {
        isNotNull
        for (token in actual) {
            if (token.value == tokenValue && token.type === tokenType) {
                return this
            }
        }
        throw failure("Expected to have token '$tokenValue' of type $tokenType but was not found")
    }
    
    public fun hasToken(tokenValue: String): TokenListAssert {
        isNotNull
        for (token in actual) {
            if (token.value == tokenValue) {
                return this
            }
        }
        throw failure("Expected to have token '$tokenValue' but was not found")
    }

    public fun hasOriginalToken(tokenValue: String): TokenListAssert {
        isNotNull
        for (token in actual) {
            if (token.originalValue == tokenValue) {
                return this
            }
        }
        throw failure("Expected to have original token '$tokenValue' but was not found")
    }

    public fun hasToken(tokenType: TokenType): TokenListAssert {
        isNotNull
        for (token in actual) {
            if (token.type === tokenType) {
                return this
            }
        }
        throw failure("Expected to have token of type $tokenType but was not found")
    }

    public fun hasTokens(vararg tokenValues: String): TokenListAssert {
        isNotNull
        if (actual.size != tokenValues.size) {
            throw failure("Expected to have ${tokenValues.size} tokens but found ${actual.size}")
        }
        for (i in actual.indices) {
            val token = actual[i]
            if (token.value != tokenValues[i]) {
                throw failure("Expected token '$tokenValues[i]' but found '${token.value}'")
            }
        }
        return this
    }

    public fun hasLastToken(tokenValue: String, tokenType: TokenType): TokenListAssert {
        isNotNull
        if (actual.isEmpty()) {
            throw failure("There must be at least one lexed token.")
        }
        val lastToken = actual[actual.size - 1]
        if (lastToken.value != tokenValue || lastToken.type !== tokenType) {
            throw failure("Expected to have last token '$tokenValue' of type $tokenType but was not found")
        }
        return this
    }

    @JvmOverloads
    public fun hasComment(commentValue: String, commentLine: Int = -1): TokenListAssert {
        isNotNull
        for (token in actual) {
            for (trivia in token.trivia) {
                if (trivia.isComment && trivia.token.value == commentValue &&
                    (commentLine > -1 && trivia.token.line == commentLine)) {
                    return this
                }
            }
        }
        return this
    }

    @JvmOverloads
    public fun hasOriginalComment(commentValue: String, commentLine: Int = -1): TokenListAssert {
        isNotNull
        for (token in actual) {
            for (trivia in token.trivia) {
                if (trivia.isComment && trivia.token.originalValue == commentValue &&
                    (commentLine > -1 && trivia.token.line == commentLine)) {
                    return this
                }
            }
        }
        return this
    }

}
