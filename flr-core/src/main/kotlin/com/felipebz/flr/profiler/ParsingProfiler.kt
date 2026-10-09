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

import com.felipebz.flr.internal.vm.CompiledGrammar
import com.felipebz.flr.internal.vm.InstructionProgram
import com.felipebz.flr.internal.vm.ProgramCounters
import java.util.IdentityHashMap

/**
 * Opt-in collector of rule and memoization *event counts*, cheap enough to run over thousands of files.
 *
 * It records counts only: no wall-clock or CPU time, no events, no objects per call. Parsing through
 * `Parser.parse(..., profiler)` or `ParseRunner.parse(..., profiler)` runs a specialized profiling VM; every
 * other parse uses the ordinary VM and is unaffected.
 *
 * ```
 * val profiler = ParsingProfiler()
 * for (file in files) parser.parse(file, profiler)
 * val snapshot = profiler.snapshot()
 * snapshot.programs.flatMap { it.targets }.sortedByDescending { it.memoHits }.take(10)
 * ```
 *
 * Counters live in one array per compiled program, created the first time the profiler meets the program, and
 * are shared by all parses on it; a profiled parse allocates no profiler state of its own. Memory is therefore
 * proportional to the number of call targets of each profiled program, not to input size or parse count. The
 * profiler keeps its programs alive, so discard it when the parsers it profiled are no longer needed.
 *
 * **Not thread-safe.** Counters are plain (unsynchronized) fields. Confine a profiler to one thread at a time;
 * to profile concurrent parses use one profiler per worker. Their snapshots are independent observations of
 * independently registered programs: numeric ids are not comparable across profilers, so combining them needs
 * a correspondence you establish yourself (see [ProfileSnapshot]).
 *
 * @since 1.7
 */
public class ParsingProfiler {
    private val countersByProgram = IdentityHashMap<InstructionProgram, ProgramCounters>()
    private val ordered = ArrayList<ProgramCounters>()

    /** Looked up once per parse, never per instruction. */
    internal fun countersFor(grammar: CompiledGrammar): ProgramCounters {
        return countersByProgram.getOrPut(grammar.program) {
            ProgramCounters(grammar, ordered.size).also { ordered.add(it) }
        }
    }

    /**
     * Copies the current counters; later parses do not change the returned snapshot. Snapshots of one profiler
     * are cumulative (each includes everything recorded since the profiler was created or last [reset]), so
     * do not sum snapshots taken at different times.
     */
    public fun snapshot(): ProfileSnapshot {
        return ProfileSnapshot(ordered.map { it.toProfile() })
    }

    /**
     * Zeroes all counters, and so starts a new observation interval. Registered programs, their ids and their
     * metadata are kept.
     */
    public fun reset() {
        ordered.forEach { it.reset() }
    }
}
