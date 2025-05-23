/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2025 Felipe Zorzo
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
package com.felipebz.flr.api

import com.felipebz.flr.grammar.GrammarException
import com.felipebz.flr.grammar.GrammarRuleKey
import com.felipebz.flr.impl.matcher.RuleDefinition
import com.felipebz.flr.internal.grammar.MutableParsingRule
import com.felipebz.flr.parser.LexerlessGrammar
import java.lang.reflect.Field

/**
 * Use [com.felipebz.flr.grammar.LexerfulGrammarBuilder] to create instances of this class.
 *
 *
 * This class is not intended to be instantiated or subclassed by clients.
 */
public abstract class Grammar {
    init {
        instanciateRuleFields()
    }

    private fun instanciateRuleFields() {
        for (ruleField in getAllRuleFields(this.javaClass)) {
            val ruleName = ruleField.name
            try {
                val rule = if (this is LexerlessGrammar) {
                    MutableParsingRule(ruleName)
                } else {
                    RuleDefinition(ruleName)
                }
                ruleField.isAccessible = true
                ruleField[this] = rule
            } catch (e: Exception) {
                throw GrammarException(e, "Unable to instanciate the rule '" + ruleName + "': " + e.message)
            }
        }
    }

    /**
     * Allows to obtain an instance of grammar rule, which was constructed by
     * [com.felipebz.flr.grammar.LexerlessGrammarBuilder] and [com.felipebz.flr.grammar.LexerfulGrammarBuilder].
     *
     * @since 1.18
     */
    public open fun rule(ruleKey: GrammarRuleKey): Rule {
        throw UnsupportedOperationException()
    }

    /**
     * Each Grammar has always an entry point whose name is usually by convention the "Computation Unit".
     *
     * @return the entry point of this Grammar
     */
    public abstract val rootRule: Rule

    public companion object {
        /**
         * Find all the direct rule fields declared in the given Grammar class.
         * Inherited rule fields are not returned.
         *
         * @param grammarClass
         * the class of the Grammar for which rule fields must be found
         * @return the rule fields declared in this class, excluding the inherited ones
         * @see getAllRuleFields
         */
        @JvmStatic
        public fun getRuleFields(grammarClass: Class<*>): MutableList<Field> {
            val fields = grammarClass.declaredFields
            val ruleFields = mutableListOf<Field>()
            for (field in fields) {
                if (Rule::class.java.isAssignableFrom(field.type)) {
                    ruleFields.add(field)
                }
            }
            return ruleFields
        }

        /**
         * Find all direct and indirect rule fields declared in the given Grammar class.
         * Inherited rule fields are also returned.
         *
         * @param grammarClass
         * the class of the Grammar for which rule fields must be found
         * @return the rule fields declared in this class, as well as the inherited ones
         * @see getRuleFields
         */
        @JvmStatic
        public fun getAllRuleFields(grammarClass: Class<*>): List<Field> {
            val ruleFields = getRuleFields(grammarClass)
            var superClass = grammarClass.superclass
            while (superClass != null) {
                ruleFields.addAll(getRuleFields(superClass))
                superClass = superClass.superclass
            }
            return ruleFields
        }
    }
}
