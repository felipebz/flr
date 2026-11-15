//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

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
