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

private val EMPTY_PARSE_NODES = emptyArray<ParseNode>()

/**
 * VM selected once for compiled grammars that contain parser-context expressions.
 * Context checkpoints and context-bearing memo entries deliberately live outside
 * [MachineStack], leaving the ordinary VM's frames and memo representation unchanged.
 * Until context is first activated, this machine uses the ordinary memo representation
 * and avoids allocating or writing context snapshots.
 *
 * Open only so that [ProfilingContextAwareMachine] and [RetainingContextAwareMachine] can specialize it; the three fields they must read
 * are [JvmField]s so the ordinary machine's bytecode keeps direct field access.
 */
internal open class ContextAwareMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler
) : Machine(input, tokens, program, handler, true) {
    @JvmField internal var context: ParsingContext = ParsingContext.EMPTY
    // Machine.execute pushes the root frame directly; start at its resulting depth.
    private var contextDepth = 2
    @JvmField internal var contextEverActivated: Boolean = false
    private var contextSnapshots: Array<ParsingContext?>? = null
    private val memoCapacity: Int = (if (input.isNotEmpty()) input.size else tokens.size) + 1
    @JvmField internal var memoContexts: Array<ParsingContext?>? = null

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
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
            val callState = index + 1
            if (calls[targetId] == callState) {
                throw GrammarException("Left recursion has been detected, involved rule: " + matcher.toString())
            }
            stack.calledTargetId = targetId
            stack.previousCallState = calls[targetId]
            calls[targetId] = callState
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
        val subNodes = stack.subNodes
        val children = when (subNodes.size) {
            0 -> EMPTY_PARSE_NODES
            1 -> arrayOf(subNodes[0])
            2 -> arrayOf(subNodes[0], subNodes[1])
            3 -> arrayOf(subNodes[0], subNodes[1], subNodes[2])
            else -> subNodes.toTypedArray()
        }
        val node = ParseNode(stack.index, index, stack.matcher, children)
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
