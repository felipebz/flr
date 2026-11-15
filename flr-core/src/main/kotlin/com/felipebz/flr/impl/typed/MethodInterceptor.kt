//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.typed

import java.lang.reflect.Method

public fun interface MethodInterceptor {
    /**
     * @return `true` if original method should not be invoked
     */
    public fun intercept(method: Method): Boolean
}
