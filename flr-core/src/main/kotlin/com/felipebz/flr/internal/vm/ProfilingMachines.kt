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
import com.felipebz.flr.internal.matchers.Matcher

/**
 * Profiling counterparts of [Machine] and [ContextAwareMachine], selected once per profiled parse.
 *
 * Each override classifies what the ordinary implementation is about to do, increments a slot of the shared
 * [ProgramCounters.counters], then delegates to the ordinary implementation, so the profiler never influences
 * a decision. Nothing is allocated per machine or per event.
 *
 * Hooks: the memo lookup in `pushReturn` (hit, or miss by cause) and `createNode` (match, and replacement of
 * another matcher's memo). Failures are derived from these counts and need no hook.
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
        val kind = if (memo == null) ProgramCounters.EMPTY_MISSES
        else if (memo.matcher === matcher) ProgramCounters.HITS
        else ProgramCounters.MATCHER_MISSES
        counters[targetId * ProgramCounters.STRIDE + kind]++
        super.pushReturn(returnOffset, matcher, callOffset, targetId)
    }

    override fun createNode() {
        val frame = stack
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
        val kind = if (memo == null) ProgramCounters.EMPTY_MISSES
        else if (memo.matcher !== matcher) ProgramCounters.MATCHER_MISSES
        // same predicate as ContextAwareMachine.pushReturn
        else if (!contextEverActivated || memoContexts?.get(index) == context) ProgramCounters.HITS
        else ProgramCounters.CONTEXT_MISSES
        counters[targetId * ProgramCounters.STRIDE + kind]++
        super.pushReturn(returnOffset, matcher, callOffset, targetId)
    }

    override fun createNode() {
        val frame = stack
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
        super.createNode()
    }
}
