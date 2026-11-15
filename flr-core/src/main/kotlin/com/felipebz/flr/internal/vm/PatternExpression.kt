//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.grammar.GrammarException
import java.util.regex.Matcher
import java.util.regex.Pattern

public class PatternExpression(regex: String) : NativeExpression(), com.felipebz.flr.internal.matchers.Matcher {
    /** Visible for testing. */
    internal val matcher: Matcher = Pattern.compile(regex).matcher("")

    /**
     * @throws GrammarException if execution of regular expression has led to StackOverflowError
     */
    override fun execute(machine: Machine) {
        matcher.reset(machine)
        val result: Boolean = try {
            matcher.lookingAt()
        } catch (e: StackOverflowError) {
            throw GrammarException(
                e, "The regular expression '" + matcher.pattern().pattern() + "' has led to a stack overflow error."
                        + " This error is certainly due to an inefficient use of alternations. See https://bugs.java.com/bugdatabase/view_bug.do?bug_id=5050507"
            )
        }
        if (result) {
            // An empty match (`\s*` on no blank) is a leaf of no characters.
            machine.createLeafNode(this, matcher.end())
            machine.jump(1)
        } else {
            machine.backtrack()
        }

        // Avoid keeping a reference to the "Machine" instance:
        matcher.reset("")
    }

    override fun toString(): String {
        return "Pattern " + matcher.pattern().pattern()
    }

}
