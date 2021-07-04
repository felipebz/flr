/**
 * FLR
 * Copyright (C) 2010-2021 SonarSource SA
 * Copyright (C) 2021-2021 Felipe Zorzo
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
package org.sonar.sslr.internal.vm

import com.sonar.sslr.api.RecognitionException
import com.sonar.sslr.api.Token
import org.sonar.sslr.grammar.GrammarException
import org.sonar.sslr.internal.matchers.ImmutableInputBuffer
import org.sonar.sslr.internal.matchers.InputBuffer
import org.sonar.sslr.internal.matchers.Matcher
import org.sonar.sslr.internal.matchers.ParseNode
import org.sonar.sslr.internal.vm.lexerful.LexerfulParseErrorFormatter
import org.sonar.sslr.parser.ParseError
import org.sonar.sslr.parser.ParsingResult

class Machine private constructor(
    private val input: CharArray,
    private val tokens: Array<out Token>,
    instructions: Array<Instruction>,
    private val handler: MachineHandler
) : CharSequence {
    private var inputLength = if (input.isNotEmpty()) input.size else tokens.size
    private var stack = MachineStack()
    private var matched = true
    private val memos: Array<ParseNode?> = arrayOfNulls(inputLength + 1)

    // Number of instructions in grammar for Java is about 2000.
    private val calls: IntArray = IntArray(instructions.size)

    var address = 0
    var index = 0
    var ignoreErrors = false

    private fun execute(matcher: Matcher?, offset: Int, instructions: Array<Instruction>) {
        // Place first rule on top of stack
        push(-1)
        stack.matcher = matcher
        jump(offset)
        execute(instructions)
    }

    @JvmOverloads
    constructor(
        input: String,
        instructions: Array<Instruction>,
        handler: MachineHandler = NOP_HANDLER
    ) : this(input.toCharArray(), emptyArray(), instructions, handler)

    private fun execute(instructions: Array<Instruction>) {
        while (address != -1) {
            instructions[address].execute(this)
        }
    }

    fun jump(offset: Int) {
        address += offset
    }

    private fun push(address: Int) {
        stack = stack.getOrCreateChild()
        stack.subNodes.clear()
        stack.address = address
        stack.index = index
        stack.ignoreErrors = ignoreErrors
    }

    fun popReturn() {
        calls[stack.calledAddress] = stack.leftRecursion
        stack = stack.parent()
    }

    fun pushReturn(returnOffset: Int, matcher: Matcher?, callOffset: Int) {
        val memo = memos[index]
        if (memo != null && memo.matcher === matcher) {
            stack.subNodes.add(memo)
            index = memo.endIndex
            address += returnOffset
        } else {
            push(address + returnOffset)
            stack.matcher = matcher
            address += callOffset
            if (calls[address] == index) {
                // TODO better message, e.g. dump stack
                throw GrammarException("Left recursion has been detected, involved rule: " + matcher.toString())
            }
            stack.calledAddress = address
            stack.leftRecursion = calls[address]
            calls[address] = index
        }
    }

    fun pushBacktrack(offset: Int) {
        push(address + offset)
        stack.matcher = null
    }

    fun pop() {
        stack = stack.parent()
    }

    fun peek(): MachineStack {
        return stack
    }

    fun backtrack() {
        // pop any return addresses from the top of the stack
        while (stack.isReturn()) {

            // TODO we must have this inside of loop, otherwise report won't be generated in case of input "foo" and rule "nextNot(foo)"
            ignoreErrors = stack.ignoreErrors
            if (!ignoreErrors) {
                handler.onBacktrack(this)
            }
            popReturn()
        }
        if (stack.isEmpty()) {
            // input does not match
            address = -1
            matched = false
        } else {
            // restore state
            index = stack.index
            address = stack.address
            ignoreErrors = stack.ignoreErrors
            stack = stack.parent()
        }
    }

    fun createNode() {
        val node = ParseNode(stack.index, index, stack.matcher, stack.subNodes)
        stack.parent().subNodes.add(node)
        if (stack.matcher is MemoParsingExpression && (stack.matcher as MemoParsingExpression).shouldMemoize()) {
            memos[stack.index] = node
        }
    }

    fun createLeafNode(matcher: Matcher?, offset: Int) {
        val node = ParseNode(index, index + offset, matcher)
        stack.subNodes.add(node)
        index += offset
    }

    fun advanceIndex(offset: Int) {
        index += offset
    }

    override val length: Int
        get() {
            return inputLength - index
        }

    override fun get(index: Int): Char {
        return input[this.index + index]
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always
     */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        throw UnsupportedOperationException()
    }

    fun tokenAt(offset: Int): Token {
        return tokens[index + offset]
    }

    companion object {
        @JvmStatic
        fun parse(tokens: List<Token>, grammar: CompiledGrammar): ParseNode {
            val inputTokens: Array<Token> = tokens.toTypedArray()
            val errorLocatingHandler = ErrorLocatingHandler()
            val machine = Machine(CharArray(0), inputTokens, grammar.instructions, errorLocatingHandler)
            machine.execute(grammar.getMatcher(grammar.rootRuleKey), grammar.rootRuleOffset, grammar.instructions)
            return if (machine.matched) {
                machine.stack.subNodes[0]
            } else {
                if (tokens.isEmpty()) {
                    // Godin: weird situation - I expect that list of tokens contains at least EOF, but this is not the case in C Parser
                    throw RecognitionException(1, "No tokens")
                } else {
                    val errorIndex = errorLocatingHandler.getErrorIndex()
                    val errorMsg = LexerfulParseErrorFormatter().format(tokens, errorIndex)
                    val errorLine =
                        if (errorIndex < tokens.size) tokens[errorIndex].line else tokens[tokens.size - 1].line
                    throw RecognitionException(errorLine, errorMsg)
                }
            }
        }

        @JvmStatic
        fun parse(input: CharArray, grammar: CompiledGrammar): ParsingResult {
            val instructions = grammar.instructions
            val errorLocatingHandler = ErrorLocatingHandler()
            val machine = Machine(input, emptyArray(), instructions, errorLocatingHandler)
            machine.execute(grammar.getMatcher(grammar.rootRuleKey), grammar.rootRuleOffset, instructions)
            return if (machine.matched) {
                ParsingResult(
                    ImmutableInputBuffer(checkNotNull(machine.input)),
                    machine.matched,  // TODO what if there is no nodes, or more than one?
                    machine.stack.subNodes[0],
                    null
                )
            } else {
                val inputBuffer: InputBuffer = ImmutableInputBuffer(checkNotNull(machine.input))
                val parseError = ParseError(inputBuffer, errorLocatingHandler.getErrorIndex())
                ParsingResult(inputBuffer, machine.matched, null, parseError)
            }
        }

        @JvmStatic
        fun execute(input: String, instructions: Array<Instruction>): Boolean {
            val machine = Machine(input, instructions)
            while (machine.address != -1 && machine.address < instructions.size) {
                instructions[machine.address].execute(machine)
            }
            return machine.matched
        }

        @JvmStatic
        fun execute(instructions: Array<Instruction>, vararg input: Token): Boolean {
            val machine = Machine(CharArray(0), input, instructions, NOP_HANDLER)
            while (machine.address != -1 && machine.address < instructions.size) {
                instructions[machine.address].execute(machine)
            }
            return machine.matched
        }

        @JvmStatic
        private val NOP_HANDLER = object : MachineHandler {
            override fun onBacktrack(machine: Machine) {
                // nop
            }
        }
    }

    init {
        stack = stack.getOrCreateChild()
        stack.index = -1
        calls.fill(-1)
    }
}