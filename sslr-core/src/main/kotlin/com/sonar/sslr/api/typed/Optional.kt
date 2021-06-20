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
package com.sonar.sslr.api.typed

import java.util.*

/**
 * Loosely modeled after [java.util.Optional].
 *
 * @since 1.21
 */
abstract class Optional<T> {
    abstract val isPresent: Boolean
    abstract fun get(): T
    abstract fun or(defaultValue: T): T
    abstract fun orNull(): T?
    private class Present<T>(private val reference: T) : Optional<T>() {
        override val isPresent: Boolean
            get() {
                return true
            }

        override fun get(): T {
            return reference
        }

        override fun or(defaultValue: T): T {
            requireNotNull(defaultValue) { "use orNull() instead of or(null)" }
            return reference
        }

        override fun orNull(): T? {
            return reference
        }

        override fun equals(`object`: Any?): Boolean {
            if (`object` is Present<*>) {
                return reference == `object`.reference
            }
            return false
        }

        override fun hashCode(): Int {
            return 0x598df91c + reference.hashCode()
        }

        override fun toString(): String {
            return "Optional.of($reference)"
        }
    }

    private class Absent : Optional<Any?>() {
        override val isPresent: Boolean
            get( ){
                return false
            }

        override fun get(): Any? {
            throw IllegalStateException("value is absent")
        }

        override fun or(defaultValue: Any?): Any {
            return requireNotNull(defaultValue) { "use orNull() instead of or(null)" }
        }

        override fun orNull(): Any? {
            return null
        }

        override fun equals(`object`: Any?): Boolean {
            return `object` === this
        }

        override fun hashCode(): Int {
            return 0x598df91c
        }

        override fun toString(): String {
            return "Optional.absent()"
        }

        companion object {
            val INSTANCE = Absent()
        }
    }

    companion object {
        @JvmStatic
        fun <T> absent(): Optional<T> {
            return Absent.INSTANCE as Optional<T>
        }

        @JvmStatic
        fun <T> of(reference: T): Optional<T> {
            return Present(Objects.requireNonNull(reference))
        }
    }
}