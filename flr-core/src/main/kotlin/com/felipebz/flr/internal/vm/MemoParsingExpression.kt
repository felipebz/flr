//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.vm

import com.felipebz.flr.internal.matchers.Matcher

public interface MemoParsingExpression : ParsingExpression, Matcher {
    public fun shouldMemoize(): Boolean

    /**
     * Whether successful matches stay reusable after another rule's match replaces them in the memo.
     * Only honoured when [shouldMemoize] is also true.
     *
     * @since 1.7
     */
    public fun shouldRetainMemo(): Boolean = false
}
