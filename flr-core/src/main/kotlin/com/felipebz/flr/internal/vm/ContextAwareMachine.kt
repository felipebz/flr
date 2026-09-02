/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
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
package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.Token
import com.felipebz.flr.grammar.ContextKey
import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.matchers.ParseNode

/**
 * VM selected once for compiled grammars that contain parser-context expressions.
 * Context checkpoints and context-bearing memo entries deliberately live outside
 * [MachineStack], leaving the ordinary VM's frames and memo representation unchanged.
 * Until context is first activated, this machine uses the ordinary memo representation
 * and avoids allocating or writing context snapshots.
 */
internal class ContextAwareMachine(
    input: CharArray,
    tokens: Array<out Token>,
    instructions: Array<Instruction>,
    handler: MachineHandler
) : Machine(input, tokens, instructions, handler, true) {
    private var context: ParsingContext = ParsingContext.EMPTY
    // Machine.execute pushes the root frame directly; start at its resulting depth.
    private var contextDepth = 2
    private var contextEverActivated: Boolean = false
    private var contextSnapshots: Array<ParsingContext?>? = null
    private val memoCapacity: Int = (if (input.isNotEmpty()) input.size else tokens.size) + 1
    private var memoContexts: Array<ParsingContext?>? = null

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int) {
        val memoNode = memos[index]?.takeIf {
            it.matcher === matcher &&
                (!contextEverActivated || memoContexts?.get(index) == context)
        }
        if (memoNode != null) {
            stack.subNodes.add(memoNode)
            index = memoNode.endIndex
            address += returnOffset
        } else {
            pushWithContext(address + returnOffset)
            stack.matcher = matcher
            address += callOffset
            if (calls[address] == index) {
                throw GrammarException("Left recursion has been detected, involved rule: " + matcher.toString())
            }
            stack.calledAddress = address
            stack.leftRecursion = calls[address]
            calls[address] = index
        }
    }

    override fun pushBacktrack(offset: Int) {
        pushWithContext(address + offset)
        stack.matcher = null
    }

    private fun pushWithContext(address: Int) {
        push(address)
        contextDepth++
        if (contextEverActivated) {
            ensureSnapshotCapacity()
            checkNotNull(contextSnapshots)[contextDepth] = context
        }
    }

    override fun popReturn() {
        super.popReturn()
        contextDepth--
    }

    override fun pop() {
        super.pop()
        contextDepth--
    }

    override fun backtrack() {
        while (stack.isReturn()) {
            ignoreErrors = stack.ignoreErrors
            if (!ignoreErrors) {
                handler.onBacktrack(this)
            }
            popReturn()
        }
        if (stack.isEmpty()) {
            context = ParsingContext.EMPTY
            address = -1
            matched = false
        } else {
            index = stack.index
            address = stack.address
            ignoreErrors = stack.ignoreErrors
            restoreContextFromCheckpoint()
            stack = stack.parent()
            contextDepth--
        }
    }

    override fun createNode() {
        val node = ParseNode(stack.index, index, stack.matcher, stack.subNodes.toTypedArray())
        stack.parent().subNodes.add(node)
        val matcher = stack.matcher
        if (matcher is MemoParsingExpression && matcher.shouldMemoize()) {
            memos[stack.index] = node
            if (contextEverActivated) {
                checkNotNull(memoContexts)[stack.index] = contextAtCheckpoint()
            }
        }
    }

    override fun enterContext(key: ContextKey<*>, value: Any?, present: Boolean) {
        // Masking a key in EMPTY cannot change any predicate result. Keeping this
        // scope as EMPTY also lets ordinary top-level isolation scopes stay on the
        // never-activated memo and checkpoint paths.
        if (!present && context === ParsingContext.EMPTY) {
            return
        }
        if (!contextEverActivated) {
            contextEverActivated = true
            memos.fill(null)
            memoContexts = arrayOfNulls(memoCapacity)
            ensureSnapshotCapacity()
        }
        context = if (present) context.with(key, value) else context.without(key)
    }

    override fun exitContext() {
        if (context !== ParsingContext.EMPTY) {
            context = context.parent()
        }
    }

    override fun containsContext(key: ContextKey<*>): Boolean {
        return context.contains(key)
    }

    override fun matchesContext(key: ContextKey<*>, expected: Any?): Boolean {
        return context.matches(key, expected)
    }

    override fun restoreContextFromCheckpoint() {
        if (contextEverActivated) {
            context = contextAtCheckpoint()
        }
    }

    private fun contextAtCheckpoint(): ParsingContext {
        return contextSnapshots?.get(contextDepth) ?: ParsingContext.EMPTY
    }

    private fun ensureSnapshotCapacity() {
        val snapshots = contextSnapshots
        if (snapshots == null || contextDepth >= snapshots.size) {
            var newSize = snapshots?.size ?: 64
            while (contextDepth >= newSize) {
                newSize *= 2
            }
            contextSnapshots = snapshots?.copyOf(newSize) ?: arrayOfNulls(newSize)
        }
    }

}
