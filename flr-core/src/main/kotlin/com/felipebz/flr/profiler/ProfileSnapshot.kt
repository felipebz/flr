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
package com.felipebz.flr.profiler

/**
 * Immutable copy of everything a [ParsingProfiler] has recorded so far.
 *
 * **Identity within one profiler.** Programs are listed in the order the profiler first met them.
 * [ProgramProfile.programId] distinguishes programs inside that profiler only, and [TargetProfile.targetId]
 * identifies a target inside one program only. Parses of an already registered program accumulate into the same
 * counters.
 *
 * **Snapshots are cumulative.** A snapshot includes everything recorded since the profiler was created or last
 * reset, so a later snapshot of the same profiler contains the earlier one. Summing snapshots taken at different
 * times double-counts unless you have separated the observation intervals yourself, for example with
 * [ParsingProfiler.reset].
 *
 * **Across independent profilers.** Program ids and target ids are not comparable: id 0 in one profiler can
 * be a different compiled program than id 0 in another, and equal ids inside equally numbered programs do not
 * prove the targets correspond. Two separately compiled grammars are not shown to be equivalent by similar
 * names or sizes. Exact merging needs a correspondence you establish yourself (for example, both profilers saw
 * the very same `Parser` or `ParseRunner` instance's program, or you map targets by an identifier you control).
 * Grouping by [TargetProfile.name] is a best-effort aggregation that may merge unrelated rules; keep
 * [TargetProfile.kind] `INITIAL_ROOT` entries apart from called targets when you do.
 *
 * @since 1.7
 */
public class ProfileSnapshot internal constructor(
    public val programs: List<ProgramProfile>
) {
    override fun toString(): String = "ProfileSnapshot(programs=${programs.size})"
}

/**
 * Counters for one compiled grammar program.
 *
 * Several [ProgramProfile]s can describe the same grammar: every compiled program instance is profiled
 * separately, and call-target ids are local to one program. Do not merge two programs by [TargetProfile.targetId].
 * [TargetProfile.name] is a display name and not a unique rule identifier: different rules can share it, even
 * inside one program, so a rule-level total across programs by name is only a best-effort aggregation.
 *
 * @since 1.7
 */
public class ProgramProfile internal constructor(
    /** Identifier local to the producing profiler; not comparable with ids of any other profiler. */
    public val programId: Int,
    /** Number of compiled instructions of the program. */
    public val instructionCount: Int,
    /** Number of distinct called entry addresses, which is also the number of [targets]. */
    public val callTargetCount: Int,
    /** Number of parses started on this program, i.e. initial root invocations (including parses that threw). */
    public val parses: Long,
    /** The initial root invocation. It has no call target id and never performs a memo lookup. */
    public val root: TargetProfile,
    /** Called targets; the list index is the target id. */
    public val targets: List<TargetProfile>
) {
    override fun toString(): String = "ProgramProfile(programId=$programId, callTargets=$callTargetCount, parses=$parses)"
}

/** What a [TargetProfile] describes. */
public enum class TargetKind {
    /** The initial root invocation of a parse. It is separate from the root rule's own called target, if any. */
    INITIAL_ROOT,

    /** The entry address of a grammar rule reached through call instructions. */
    CALLED_RULE,

    /** A called entry address that is not a grammar rule, e.g. a token or trivia subroutine. */
    CALLED_ANONYMOUS
}

/**
 * Counters of one initial root invocation or one called target.
 *
 * All values are event counts accumulated over every parse recorded for the program; nothing is timed.
 *
 * Terminology:
 * - a *lookup* happens at every call instruction and reads the memo slot of the current input position;
 * - a *hit* is a lookup that reuses a stored result and does not run the rule;
 * - an *executed invocation* is a lookup that did not hit, so the rule body ran (the initial root invocation
 *   is executed once per parse and performs no lookup);
 * - an executed invocation either *matches* or fails.
 *
 * The memo is one slot per input position holding the result of whichever memoizing rule stored there last,
 * so a [memoMatcherMisses] count is a rule finding another rule's result in its slot, not necessarily a
 * result that would have been reusable. For rules with memo retention
 * ([com.felipebz.flr.grammar.GrammarRuleBuilder.enableMemoRetention]), a lookup that misses the slot but reuses the
 * rule's retained result counts as a hit; the miss counters then only cover lookups that executed the rule.
 *
 * @since 1.7
 */
public class TargetProfile internal constructor(
    public val kind: TargetKind,
    /**
     * Dense call-target id, local to the enclosing [ProgramProfile] (it equals the index in
     * [ProgramProfile.targets]); `-1` for [TargetKind.INITIAL_ROOT]. Not comparable across programs or profilers.
     */
    public val targetId: Int,
    /**
     * Display name for reports (the rule's name, or the matcher type and entry address for anonymous targets). It is
     * not a unique identifier: distinct rules, even inside one program, can share it.
     */
    public val name: String,
    /** Instruction address where the target starts. */
    public val entryAddress: Int,
    /** Whether the target's matcher stores memos when it matches. For [ambiguousMatchers] targets: all matchers do. */
    public val memoizing: Boolean,
    /** Number of call instructions targeting this entry address; `0` for the initial root. */
    public val callSites: Int,
    /**
     * True when call sites of this target expose different matcher objects. [name] then joins their descriptions
     * and the derived [memoStores] may not be exact.
     */
    public val ambiguousMatchers: Boolean,
    /**
     * Lookups that reused a memo (matching matcher and, for parser-context grammars, a compatible context), including
     * retained results.
     */
    public val memoHits: Long,
    /** Lookups that found an empty slot. */
    public val memoEmptyMisses: Long,
    /** Lookups that found a slot occupied by another matcher. */
    public val memoMatcherMisses: Long,
    /** Lookups that found the requested matcher with an incompatible parser context (context-aware parses only). */
    public val memoContextMisses: Long,
    /** Executed invocations that completed successfully and created their node. */
    public val matches: Long,
    /** Memoizing matches that replaced a memo owned by another matcher at their start position. */
    public val memoOverwritesOther: Long,
    /** Executed invocations: misses for a called target, parses started for the root. */
    public val executedInvocations: Long
) {
    /** Memo lookups: hits plus all misses. Zero for the initial root. */
    public val memoLookups: Long
        get() = memoHits + memoEmptyMisses + memoMatcherMisses + memoContextMisses

    /**
     * Executed invocations that did not match. A parse aborted by an exception (for example left-recursion
     * detection) leaves started invocations neither matched nor failed, so they are counted as failures.
     */
    public val failures: Long
        get() = executedInvocations - matches

    /** Stored memos: every match of a memoizing target stores one. */
    public val memoStores: Long
        get() = if (memoizing) matches else 0L

    /** [memoHits] divided by [memoLookups], or `0.0` without lookups. */
    public val memoHitRate: Double
        get() = memoLookups.let { if (it == 0L) 0.0 else memoHits.toDouble() / it }

    override fun toString(): String =
        "TargetProfile($kind, id=$targetId, name=$name, hits=$memoHits, empty=$memoEmptyMisses, " +
            "matcher=$memoMatcherMisses, context=$memoContextMisses, matches=$matches, overwrites=$memoOverwritesOther)"
}
