//
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.api.Token
import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.internal.matchers.ParseNode

/**
 * Profiling counterparts of [Machine], [ContextAwareMachine] and the retaining machines, selected once per profiled parse.
 *
 * Each override classifies what the ordinary implementation is about to do, increments a slot of the shared
 * [ProgramCounters.counters], then delegates to the ordinary implementation, so the profiler never influences
 * a decision. Nothing is allocated per machine or per event.
 *
 * Hooks: the memo lookup in `pushReturn` (hit, or miss by cause) and `createNode` (match, and replacement of
 * another matcher's memo). Failures are derived from these counts and need no hook. A call answered by a retained
 * result counts as a hit.
 */
internal class ProfilingMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler,
    profile: ProgramCounters
) : Machine(input, tokens, program, handler, true) {
    private val counters = profile.counters
    private val rootSlot = profile.rootSlot

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
        val memo = memos[index]
        val kind = when {
            memo == null -> ProgramCounters.EMPTY_MISSES
            memo.matcher === matcher -> ProgramCounters.HITS
            else -> ProgramCounters.MATCHER_MISSES
        }
        counters[targetId * ProgramCounters.STRIDE + kind]++
        super.pushReturn(returnOffset, matcher, callOffset, targetId)
    }

    override fun createNode() {
        recordMatch(counters, rootSlot, stack, memos)
        super.createNode()
    }
}

internal class ProfilingContextAwareMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler,
    profile: ProgramCounters
) : ContextAwareMachine(input, tokens, program, handler) {
    private val counters = profile.counters
    private val rootSlot = profile.rootSlot

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
        val memo = memos[index]
        val kind = when {
            memo == null -> ProgramCounters.EMPTY_MISSES
            memo.matcher !== matcher -> ProgramCounters.MATCHER_MISSES
            // same predicate as ContextAwareMachine.pushReturn
            !contextEverActivated || memoContexts?.get(index) == context -> ProgramCounters.HITS
            else -> ProgramCounters.CONTEXT_MISSES
        }
        counters[targetId * ProgramCounters.STRIDE + kind]++
        super.pushReturn(returnOffset, matcher, callOffset, targetId)
    }

    override fun createNode() {
        recordMatch(counters, rootSlot, stack, memos)
        super.createNode()
    }
}

internal class ProfilingRetainingMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler,
    retention: MemoRetention,
    profile: ProgramCounters
) : RetainingMachine(input, tokens, program, handler, retention) {
    private val counters = profile.counters
    private val rootSlot = profile.rootSlot

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
        val memo = memos[index]
        val kind = when {
            memo != null && memo.matcher === matcher -> ProgramCounters.HITS
            retainedHit(matcher, targetId) != null -> ProgramCounters.HITS
            memo == null -> ProgramCounters.EMPTY_MISSES
            else -> ProgramCounters.MATCHER_MISSES
        }
        counters[targetId * ProgramCounters.STRIDE + kind]++
        super.pushReturn(returnOffset, matcher, callOffset, targetId)
    }

    override fun createNode() {
        recordMatch(counters, rootSlot, stack, memos)
        super.createNode()
    }
}

internal class ProfilingRetainingContextAwareMachine(
    input: CharArray,
    tokens: Array<out Token>,
    program: InstructionProgram,
    handler: MachineHandler,
    retention: MemoRetention,
    profile: ProgramCounters
) : RetainingContextAwareMachine(input, tokens, program, handler, retention) {
    private val counters = profile.counters
    private val rootSlot = profile.rootSlot

    override fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int, targetId: Int) {
        val memo = memos[index]
        val kind = when {
            retainedHit(matcher, targetId) != null -> ProgramCounters.HITS
            memo == null -> ProgramCounters.EMPTY_MISSES
            memo.matcher !== matcher -> ProgramCounters.MATCHER_MISSES
            // same predicate as ContextAwareMachine.pushReturn
            !contextEverActivated || memoContexts?.get(index) == context -> ProgramCounters.HITS
            else -> ProgramCounters.CONTEXT_MISSES
        }
        counters[targetId * ProgramCounters.STRIDE + kind]++
        super.pushReturn(returnOffset, matcher, callOffset, targetId)
    }

    override fun createNode() {
        recordMatch(counters, rootSlot, stack, memos)
        super.createNode()
    }
}

/** Counts the match of [frame] and whether its memo store replaces another matcher's memo. */
private fun recordMatch(counters: LongArray, rootSlot: Int, frame: MachineStack, memos: Array<ParseNode?>) {
    val id = frame.calledTargetId
    val base = (if (id >= 0) id else rootSlot) * ProgramCounters.STRIDE
    counters[base + ProgramCounters.MATCHES]++
    val matcher = frame.matcher
    if (matcher is MemoParsingExpression && matcher.shouldMemoize()) {
        val previous = memos[frame.index]
        if (previous != null && previous.matcher !== matcher) {
            counters[base + ProgramCounters.OVERWRITES_OTHER]++
        }
    }
}
