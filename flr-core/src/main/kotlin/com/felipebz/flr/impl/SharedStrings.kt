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
package com.felipebz.flr.impl

/**
 * One string per distinct text, for the tokens of one lexed source. A [TokenTextBuilder] is looked up by its
 * characters and the hash it kept, so a text met again is found without creating a string for it. Not thread-safe:
 * one instance belongs to one [LexerOutput].
 */
internal class SharedStrings {
    // Open addressing with linear probing; the capacity is a power of two, at most half full. The hash of each string
    // is kept beside it, so that a probe reads a string only when the hashes match. A result may be kept for a string,
    // with the key it was computed for, in arrays created on the first one.
    private var strings = arrayOfNulls<String>(INITIAL_CAPACITY)
    private var hashes = IntArray(INITIAL_CAPACITY)
    private var resultKeys: Array<Any?>? = null
    private var results: Array<Any?>? = null
    private var size = 0

    fun share(text: CharSequence): String {
        // The slot may grow the table: read the array only after it.
        val index = slot(text)
        return checkNotNull(strings[index])
    }

    fun <T : Any> shareResult(text: CharSequence, key: Any, output: LexerOutput, compute: LexerOutput.(String) -> T): T {
        var index = slot(text)
        if (resultKeys?.get(index) === key) {
            @Suppress("UNCHECKED_CAST")
            return checkNotNull(results)[index] as T
        }
        val string = checkNotNull(strings[index])
        val result = output.compute(string)
        // Computing may have added strings, and so moved this one.
        if (strings[index] !== string) {
            index = indexOf(string, string.hashCode())
        }
        val keys = resultKeys ?: arrayOfNulls<Any>(strings.size).also { resultKeys = it }
        val values = results ?: arrayOfNulls<Any>(strings.size).also { results = it }
        keys[index] = key
        values[index] = result
        return result
    }

    /** The index of the string equal to [text], added if there is none. */
    private fun slot(text: CharSequence): Int {
        val hash = when (text) {
            is TokenTextBuilder -> text.hash
            is String -> text.hashCode()
            else -> hashOf(text)
        }
        val mask = strings.size - 1
        var index = spread(hash) and mask
        while (true) {
            val existing = strings[index] ?: break
            if (hashes[index] == hash && sameText(existing, text)) {
                return index
            }
            index = (index + 1) and mask
        }
        val string = text.toString()
        strings[index] = string
        hashes[index] = hash
        if (++size * 2 > strings.size) {
            grow()
            return indexOf(string, hash)
        }
        return index
    }

    private fun indexOf(string: String, hash: Int): Int {
        val mask = strings.size - 1
        var index = spread(hash) and mask
        while (strings[index] !== string) {
            index = (index + 1) and mask
        }
        return index
    }

    private fun grow() {
        val oldStrings = strings
        val oldHashes = hashes
        val oldKeys = resultKeys
        val oldResults = results
        val capacity = oldStrings.size * 2
        strings = arrayOfNulls(capacity)
        hashes = IntArray(capacity)
        resultKeys = oldKeys?.let { arrayOfNulls(capacity) }
        results = oldResults?.let { arrayOfNulls(capacity) }
        val mask = capacity - 1
        for (i in oldStrings.indices) {
            val string = oldStrings[i] ?: continue
            var index = spread(oldHashes[i]) and mask
            while (strings[index] != null) {
                index = (index + 1) and mask
            }
            strings[index] = string
            hashes[index] = oldHashes[i]
            if (oldKeys != null && oldResults != null) {
                checkNotNull(resultKeys)[index] = oldKeys[i]
                checkNotNull(results)[index] = oldResults[i]
            }
        }
    }

    private companion object {
        const val INITIAL_CAPACITY = 256

        /** The same hash as [String.hashCode], so that any text is looked up like an equal string. */
        fun hashOf(text: CharSequence): Int {
            var hash = 0
            for (i in 0 until text.length) {
                hash = 31 * hash + text[i].code
            }
            return hash
        }

        fun sameText(string: String, text: CharSequence): Boolean =
            if (text is TokenTextBuilder) text.contentEquals(string) else string.contentEquals(text)

        fun spread(hash: Int): Int = hash xor (hash ushr 16)
    }
}
