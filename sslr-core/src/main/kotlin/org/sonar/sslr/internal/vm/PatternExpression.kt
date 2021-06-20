/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.sslr.internal.vm

import org.sonar.sslr.grammar.GrammarException
import java.util.regex.Matcher
import java.util.regex.Pattern

class PatternExpression(regex: String) : NativeExpression(), org.sonar.sslr.internal.matchers.Matcher {
    private val matcher: Matcher = Pattern.compile(regex).matcher("")

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
            // TODO what if end == 0 ???
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

    /**
     * Visible for testing.
     */
    fun getMatcher(): Matcher {
        return matcher
    }

}