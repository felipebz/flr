/**
 * FLR
 * Copyright (C) 2010-2023 SonarSource SA
 * Copyright (C) 2021-2023 Felipe Zorzo
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
package com.felipebz.flr.impl.typed

import com.felipebz.flr.impl.typed.Interceptor.create
import org.fest.assertions.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.reflect.Method

class InterceptorTest {
    open class Target(open val p: Any) : BaseTarget() {
        open fun m(): Any {
            return "m()"
        }

        open fun overloaded(): Any {
            return "overloaded()"
        }

        open fun overloaded(p: Any): Any {
            return "overloaded($p)"
        }

        override fun overridden(): Any {
            return "Target.overridden()"
        }

        private fun privateMethod(): Any {
            return "privateMethod()"
        }
    }

    open class BaseTarget {
        open fun overridden(): Any {
            return "BaseTarget.overridden()"
        }

        open fun base(): Any {
            return "base()"
        }
    }

    private var intercept = false
    private val interceptedMethods = mutableListOf<Method>()
    private val methodInterceptor: MethodInterceptor = MethodInterceptor { method: Method ->
        interceptedMethods.add(method)
        intercept
    }
    private val interceptedTarget = create(
        Target::class.java, arrayOf(Any::class.java), arrayOf("arg"),
        methodInterceptor
    ) as Target

    @Test
    fun should_invoke_constructor() {
        assertEquals("arg", interceptedTarget.p)
    }

    @Test
    fun should_intercept() {
        assertEquals("m()", interceptedTarget.m())
        assertEquals(1, interceptedMethods.size.toLong())
        intercept = true
        assertNull(interceptedTarget.m())
        assertEquals(2, interceptedMethods.size.toLong())
    }

    @Test
    fun should_intercept_overloaded_methods() {
        assertEquals("overloaded()", interceptedTarget.overloaded())
        assertEquals(1, interceptedMethods.size.toLong())
        assertEquals("overloaded(arg)", interceptedTarget.overloaded("arg"))
        assertEquals(2, interceptedMethods.size.toLong())
    }

    @Test
    fun should_intercept_overridden_methods() {
        assertEquals("Target.overridden()", interceptedTarget.overridden())
        assertEquals(1, interceptedMethods.size.toLong())
    }

    @Test
    fun should_intercept_base_methods() {
        assertEquals("base()", interceptedTarget.base())
        assertEquals(1, interceptedMethods.size.toLong())
    }

    /**
     * Can not intercept non-public methods,
     * but should not fail in their presence,
     * because SonarTSQL uses private helper methods.
     */
    @Test
    fun can_not_intercept_non_public_methods() {
        assertEquals(0, interceptedMethods.size.toLong())
        assertEquals(
            listOf("base", "getP", "m", "overloaded", "overloaded", "overridden"),
            interceptedTarget.javaClass.declaredMethods
                .map { it.name }
                .sorted()
                .toList())
    }

    @Test
    fun requires_class_to_be_public() {
        val thrown = assertThrows<IllegalAccessError> {
            create(NonPublicClass::class.java, arrayOf(), arrayOf(), methodInterceptor)
        }
        assertThat(thrown.message) // Note that details of the message are different between JDK versions
            .startsWith("class GeneratedBySSLR cannot access its superclass com.felipebz.flr.impl.typed.InterceptorTest\$NonPublicClass")
    }

    private open class NonPublicClass

    /**
     * @see .can_not_intercept_non_public_methods
     */
    @Test
    fun requires_final_methods_to_be_non_public() {
        val thrown = assertThrows<VerifyError> {
            create(PublicFinalMethod::class.java, arrayOf(), arrayOf(), methodInterceptor)
        }
        assertThat(thrown.message) // Note that details of the message are different between JDK versions
            .startsWith("class GeneratedBySSLR overrides final method")
    }

    open class PublicFinalMethod {
        fun m(): Any? {
            return null
        }
    }

    @Test
    fun requires_non_primitive_return_types() {
        assertThrows<UnsupportedOperationException> {
            create(PrimitiveReturnType::class.java, arrayOf(), arrayOf(), methodInterceptor)
        }
    }

    class PrimitiveReturnType {
        fun m() {}
    }

    @Test
    fun should_use_ClassLoader_of_intercepted_class() {
        val cv = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        cv.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Target", null, "java/lang/Object", null)
        var mv: MethodVisitor = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "m", "()Ljava/lang/String;", null, null)
        mv.visitLdcInsn("m()")
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        val classBytes: ByteArray = cv.toByteArray()
        val cls = object : ClassLoader() {
            fun defineClass(): Class<*> {
                return defineClass("Target", classBytes, 0, classBytes.size)
            }
        }.defineClass()
        val interceptedTarget = create(cls, arrayOf(), arrayOf(), methodInterceptor)
        assertEquals("m()", interceptedTarget.javaClass.getMethod("m").invoke(interceptedTarget))
        assertEquals(1, interceptedMethods.size)
    }
}