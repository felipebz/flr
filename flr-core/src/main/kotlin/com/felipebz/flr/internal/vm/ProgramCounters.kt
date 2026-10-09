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

import com.felipebz.flr.internal.matchers.Matcher
import com.felipebz.flr.profiler.ProgramProfile
import com.felipebz.flr.profiler.TargetKind
import com.felipebz.flr.profiler.TargetProfile

/**
 * Profiling state shared by every profiled parse of one [InstructionProgram].
 *
 * [counters] interleaves [STRIDE] primitive counters per slot: slot `targetId` for each called target and the
 * extra slot [rootSlot] (== callTargetCount) for the initial root invocation, which has no target id.
 * The program's `callTargetCount` and the machines' call-state arrays are not affected by the extra slot.
 *
 * Target metadata is resolved once, here, from the linked [Instruction.CallInstruction]s; nothing in it is read
 * while parsing.
 */
internal class ProgramCounters(grammar: CompiledGrammar, private val programId: Int) {
    private val program: InstructionProgram = grammar.program
    private val targetCount: Int = program.callTargetCount
    @JvmField val rootSlot: Int = targetCount
    @JvmField val counters: LongArray = LongArray((targetCount + 1) * STRIDE)
    private var parses = 0L

    private val names = arrayOfNulls<String>(targetCount)
    // only targets reached through different matcher objects (never the case for compiled grammars) get aliases
    private val aliases = HashMap<Int, LinkedHashSet<String>>()
    private val entryAddresses = IntArray(targetCount) { -1 }
    private val callSites = IntArray(targetCount)
    private val memoizingAll = BooleanArray(targetCount) { true }
    private val ambiguous = BooleanArray(targetCount)
    private val isRule = BooleanArray(targetCount)

    private val rootName: String
    private val rootEntryAddress: Int = grammar.rootRuleOffset
    private val rootMemoizing: Boolean

    init {
        val firstMatcher = arrayOfNulls<Matcher>(targetCount)
        val instructions = program.instructions
        for (address in instructions.indices) {
            val call = instructions[address] as? Instruction.CallInstruction ?: continue
            val id = call.targetId
            val matcher = call.matcher
            val entry = address + call.offset
            callSites[id]++
            val memoizes = matcher is MemoParsingExpression && matcher.shouldMemoize()
            if (callSites[id] == 1) {
                firstMatcher[id] = matcher
                entryAddresses[id] = entry
                isRule[id] = matcher is CompilableGrammarRule
                names[id] = describe(matcher, entry)
                memoizingAll[id] = memoizes
            } else {
                if (matcher !== firstMatcher[id]) {
                    ambiguous[id] = true
                    aliases.getOrPut(id) { linkedSetOf(names[id]!!) }.add(describe(matcher, entry))
                }
                memoizingAll[id] = memoizingAll[id] && memoizes
            }
        }
        val root = grammar.getMatcher(grammar.rootRuleKey)
        rootName = root?.toString() ?: grammar.rootRuleKey.toString()
        rootMemoizing = root is MemoParsingExpression && root.shouldMemoize()
    }

    /** Called once per profiled parse, when its machine is created. */
    fun parseStarted() {
        parses++
    }

    fun reset() {
        counters.fill(0L)
        parses = 0L
    }

    fun toProfile(): ProgramProfile {
        val c = counters
        fun target(id: Int): TargetProfile {
            val b = id * STRIDE
            val misses = c[b + EMPTY_MISSES] + c[b + MATCHER_MISSES] + c[b + CONTEXT_MISSES]
            return TargetProfile(
                kind = if (isRule[id]) TargetKind.CALLED_RULE else TargetKind.CALLED_ANONYMOUS,
                targetId = id,
                name = aliases[id]?.joinToString(" | ") ?: names[id]!!,
                entryAddress = entryAddresses[id],
                memoizing = memoizingAll[id],
                callSites = callSites[id],
                ambiguousMatchers = ambiguous[id],
                memoHits = c[b + HITS],
                memoEmptyMisses = c[b + EMPTY_MISSES],
                memoMatcherMisses = c[b + MATCHER_MISSES],
                memoContextMisses = c[b + CONTEXT_MISSES],
                matches = c[b + MATCHES],
                memoOverwritesOther = c[b + OVERWRITES_OTHER],
                executedInvocations = misses
            )
        }
        val rb = rootSlot * STRIDE
        val root = TargetProfile(
            kind = TargetKind.INITIAL_ROOT,
            targetId = -1,
            name = rootName,
            entryAddress = rootEntryAddress,
            memoizing = rootMemoizing,
            callSites = 0,
            ambiguousMatchers = false,
            memoHits = 0L,
            memoEmptyMisses = 0L,
            memoMatcherMisses = 0L,
            memoContextMisses = 0L,
            matches = c[rb + MATCHES],
            memoOverwritesOther = c[rb + OVERWRITES_OTHER],
            executedInvocations = parses
        )
        return ProgramProfile(
            programId, program.instructions.size, targetCount, parses, root, List(targetCount) { target(it) }
        )
    }

    private fun describe(matcher: Matcher?, entry: Int): String =
        if (matcher is CompilableGrammarRule) matcher.toString()
        else "${matcher?.javaClass?.simpleName ?: "anonymous"}@$entry"

    companion object {
        const val HITS = 0
        const val EMPTY_MISSES = 1
        const val MATCHER_MISSES = 2
        const val CONTEXT_MISSES = 3
        const val MATCHES = 4
        const val OVERWRITES_OTHER = 5
        const val STRIDE = 6
    }
}
