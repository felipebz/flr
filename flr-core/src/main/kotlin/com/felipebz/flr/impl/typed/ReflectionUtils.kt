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
package com.felipebz.flr.impl.typed

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

public object ReflectionUtils {
    public fun invokeMethod(method: Method, `object`: Any?, vararg args: Any?): Any? {
        return try {
            method.invoke(`object`, *args)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }

    public fun getField(clazz: Class<*>, fieldName: String): Field {
        return try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field
        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        }
    }

    public fun setField(field: Field, instance: Any?, value: Any?) {
        try {
            field[instance] = value
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }
}
