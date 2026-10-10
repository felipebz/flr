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
package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.Token
import com.felipebz.flr.grammar.ContextKey
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.matchers.ParseNode

/**
 * Retaining matchers of one compiled program, resolved from its linked call instructions: each retaining matcher
 * owns a dense slot, and [slotByTarget] maps a call-target id to that slot or `-1`.
 */
internal class MemoRetention private constructor(
    @JvmField val slotByTarget: IntArray,
    private val matchers: Array<Matcher>
) {
    val slots: Int
        get() = matchers.size

    /** Slot of a memoized node's matcher; a linear identity scan over the (few) retaining matchers. */
    fun slotOf(matcher: Matcher?): Int {
        for (slot in matchers.indices) {
            if (matchers[slot] === matcher) return slot
        }
        return -1
    }

    companion object {
        fun of(program: InstructionProgram): MemoRetention? {
            var slotByTarget: IntArray? = null
            val matchers = ArrayList<Matcher>()
            for (instruction in program.instructions) {
                val call = instruction as? Instruction.CallInstruction ?: continue
                val matcher = call.matcher
                if (matcher !is MemoParsingExpression || !matcher.shouldMemoize() || !matcher.shouldRetainMemo()) continue
                val slots = slotByTarget ?: IntArray(program.callTargetCount) { -1 }.also { slotByTarget = it }
                var slot = matchers.indexOfFirst { it === matcher }
                if (slot < 0) {
                    slot = matchers.size
                    matchers.add(matcher)
                }
                slots[call.targetId] = slot
            }
            return slotByTarget?.let { MemoRetention(it, matchers.toTypedArray()) }
        }
    }
}

/**
 * Per-parse store of memo results displaced from the ordinary memo: for each retaining slot, at most one node per
 * input position (the most recently displaced one). Arrays are allocated per slot on its first displacement;
 * parser contexts only for nodes stored after context activation, and error-ignoring flags only once such a node exists.
 */
internal class RetainedMemos(private val retention: MemoRetention, private val capacity: Int) {
    private val nodes = arrayOfNulls<Array<ParseNode?>>(retention.slots)
    private val contexts = arrayOfNulls<Array<ParsingContext?>>(retention.slots)
    private val createdIgnoringErrors = arrayOfNulls<LongArray>(retention.slots)
    // whether the node currently in the ordinary memo slot is a retaining node created while errors were ignored
    private var occupantIgnoringErrors: LongArray? = null

    /**
     * The retained node of [slot] at [index] if it may replace an execution of [matcher]: same matcher, equal parser
     * [context] (`null` before context activation) and, unless [ignoreErrors], not created while errors were ignored.
     */
    fun find(slot: Int, index: Int, matcher: Matcher?, ignoreErrors: Boolean, context: ParsingContext?): ParseNode? {
        val node = nodes[slot]?.get(index) ?: return null
        if (node.matcher !== matcher) return null
        if (!ignoreErrors && createdIgnoringErrors[slot].has(index)) return null
        if (context != null && contexts[slot]?.get(index) != context) return null
        return node
    }

    /**
     * Called before a memoizing match overwrites the ordinary memo slot [index]: keeps [previous] if it is a retaining
     * node. [storingRetainedIgnoringErrors] describes the node about to be stored.
     */
    fun beforeStore(index: Int, previous: ParseNode?, previousContext: ParsingContext?, storingRetainedIgnoringErrors: Boolean) {
        if (previous != null) {
            val slot = retention.slotOf(previous.matcher)
            if (slot >= 0) {
                (nodes[slot] ?: arrayOfNulls<ParseNode>(capacity).also { nodes[slot] = it })[index] = previous
                if (previousContext != null) {
                    (contexts[slot] ?: arrayOfNulls<ParsingContext>(capacity).also { contexts[slot] = it })[index] =
                        previousContext
                }
                createdIgnoringErrors[slot] = set(createdIgnoringErrors[slot], index, occupantIgnoringErrors.has(index))
            }
        }
        occupantIgnoringErrors = set(occupantIgnoringErrors, index, storingRetainedIgnoringErrors)
    }

    /** Mirrors the ordinary memo being cleared when parser context is first activated. */
    fun clear() {
        nodes.fill(null)
        contexts.fill(null)
        createdIgnoringErrors.fill(null)
        occupantIgnoringErrors = null
    }

    private fun LongArray?.has(index: Int): Boolean =
        this != null && (this[index ushr 6] and (1L shl index)) != 0L

    private fun set(bits: LongArray?, index: Int, value: Boolean): LongArray? {
        if (value) {
            val b = bits ?: LongArray((capacity + 63) ushr 6)
            b[index ushr 6] = b[index ushr 6] or (1L shl index)
            return b
        }
        if (bits != null) {
            bits[index ushr 6] = bits[index ushr 6] and (1L shl index).inv()
        }
        return bits
    }
}

/**
 * [Machine] for grammars with retaining rules ([MemoParsingExpression.shouldRetainMemo]), selected once per parse.
 * A call that would miss the ordinary memo first consults the retained store; everything else is the ordinary machine.
 */
internal open class RetainingMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler,
    retention: MemoRetention
) : Machine(input, tokens, program, handler, true) {
    private val slotByTarget = retention.slotByTarget
    private val retained = RetainedMemos(retention, memos.size)

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
        val node = retainedHit(matcher, targetId)
        if (node == null) {
            super.pushReturn(returnOffset, matcher, callOffset, targetId)
        } else {
            stack.subNodes.add(node)
            index = node.endIndex
            address += returnOffset
        }
    }

    /** The retained node that replaces this call, or `null` when the ordinary memo hits or nothing is retained. */
    internal fun retainedHit(matcher: Matcher?, targetId: Int): ParseNode? {
        val slot = slotByTarget[targetId]
        if (slot < 0) return null
        val memo = memos[index]
        if (memo != null && memo.matcher === matcher) return null
        return retained.find(slot, index, matcher, ignoreErrors, null)
    }

    override fun createNode() {
        val frame = stack
        val matcher = frame.matcher
        if (matcher is MemoParsingExpression && matcher.shouldMemoize()) {
            val id = frame.calledTargetId
            retained.beforeStore(
                frame.index, memos[frame.index], null,
                frame.ignoreErrors && id >= 0 && slotByTarget[id] >= 0
            )
        }
        super.createNode()
    }
}

/** [ContextAwareMachine] counterpart of [RetainingMachine]. */
internal open class RetainingContextAwareMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler,
    retention: MemoRetention
) : ContextAwareMachine(input, tokens, program, handler) {
    private val slotByTarget = retention.slotByTarget
    private val retained = RetainedMemos(retention, memos.size)

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
        val node = retainedHit(matcher, targetId)
        if (node == null) {
            super.pushReturn(returnOffset, matcher, callOffset, targetId)
        } else {
            stack.subNodes.add(node)
            index = node.endIndex
            address += returnOffset
        }
    }

    /** Same as [RetainingMachine.retainedHit], with the ordinary memo's context predicate. */
    internal fun retainedHit(matcher: Matcher?, targetId: Int): ParseNode? {
        val slot = slotByTarget[targetId]
        if (slot < 0) return null
        val memo = memos[index]
        if (memo != null && memo.matcher === matcher &&
            (!contextEverActivated || memoContexts?.get(index) == context)
        ) return null
        return retained.find(slot, index, matcher, ignoreErrors, if (contextEverActivated) context else null)
    }

    override fun createNode() {
        val frame = stack
        val matcher = frame.matcher
        if (matcher is MemoParsingExpression && matcher.shouldMemoize()) {
            val id = frame.calledTargetId
            retained.beforeStore(
                frame.index, memos[frame.index], if (contextEverActivated) memoContexts?.get(frame.index) else null,
                frame.ignoreErrors && id >= 0 && slotByTarget[id] >= 0
            )
        }
        super.createNode()
    }

    override fun enterContext(key: ContextKey<*>, value: Any?, present: Boolean) {
        val activating = !contextEverActivated
        super.enterContext(key, value, present)
        if (activating && contextEverActivated) {
            retained.clear()
        }
    }
}
