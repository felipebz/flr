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
package com.felipebz.flr.impl.typed

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method
import java.util.*
import kotlin.streams.toList

public object Interceptor {
    @JvmStatic
    public fun create(
        superClass: Class<*>,
        constructorParameterTypes: Array<Class<*>>,
        constructorArguments: Array<Any?>,
        interceptor: MethodInterceptor?
    ): Any {
        val cv = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        val className = "GeneratedBySSLR"
        val superClassName = Type.getInternalName(superClass)
        cv.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            className,
            null,
            superClassName,
            null
        )
        cv.visitField(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "methodInterceptor",
            Type.getDescriptor(MethodInterceptor::class.java),
            null,
            null
        )
        cv.visitField(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "methods",
            Type.getDescriptor(Array<Method>::class.java),
            null,
            null
        )
        val constructorDescriptor = Type.getMethodDescriptor(
            Type.getType(Void.TYPE),
            *Arrays.stream(constructorParameterTypes)
                .map { clazz: Class<*>? -> Type.getType(clazz) }
                .toList().toTypedArray())
        var mv = cv.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            constructorDescriptor,
            null,
            null
        )
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        for (i in constructorParameterTypes.indices) {
            mv.visitVarInsn(Type.getType(constructorParameterTypes[i]).getOpcode(Opcodes.ILOAD), 1 + i)
        }
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            superClassName,
            "<init>",
            constructorDescriptor,
            false
        )
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(0, 0)
        mv.visitEnd()
        val methods = superClass.methods
        for (methodId in methods.indices) {
            val method = methods[methodId]
            if (Any::class.java == method.declaringClass) {
                continue
            }
            if (method.returnType.isPrimitive) {
                throw UnsupportedOperationException()
            }
            mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC,
                method.name,
                Type.getMethodDescriptor(method),
                null,
                null
            )
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                className,
                "methodInterceptor",
                Type.getDescriptor(MethodInterceptor::class.java)
            )
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                className,
                "methods",
                Type.getDescriptor(Array<Method>::class.java)
            )
            mv.visitLdcInsn(methodId)
            mv.visitInsn(Opcodes.AALOAD)
            mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                Type.getInternalName(MethodInterceptor::class.java),
                "intercept",
                Type.getMethodDescriptor(
                    Type.getType(Boolean::class.javaPrimitiveType),
                    Type.getType(Method::class.java)
                ),
                true
            )
            val label = Label()
            mv.visitJumpInsn(Opcodes.IFEQ, label)
            mv.visitInsn(Opcodes.ACONST_NULL)
            mv.visitInsn(Opcodes.ARETURN)
            mv.visitLabel(label)
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            val parameterTypes = method.parameterTypes
            for (i in 0 until method.parameterCount) {
                mv.visitVarInsn(Type.getType(parameterTypes[i]).getOpcode(Opcodes.ILOAD), 1 + i)
            }
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                superClassName,
                method.name,
                Type.getMethodDescriptor(method),
                false
            )
            mv.visitInsn(Opcodes.ARETURN)
            mv.visitMaxs(0, 0)
            mv.visitEnd()
        }
        val classBytes = cv.toByteArray()
        val cls = object : ClassLoader(superClass.classLoader) {
            fun defineClass(): Class<*> {
                return defineClass(className, classBytes, 0, classBytes.size)
            }
        }.defineClass()
        val instance: Any
        try {
            instance = cls
                .getConstructor(*constructorParameterTypes)
                .newInstance(*constructorArguments)
            cls.getField("methods")[instance] = methods
            cls.getField("methodInterceptor")[instance] = interceptor
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
        return instance
    }
}