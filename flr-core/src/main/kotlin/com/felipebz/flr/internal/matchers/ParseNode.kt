//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.internal.matchers

/**
 * Node of a parse tree.
 */
public class ParseNode(
    public val startIndex: Int,
    /**
     * Be aware that element of input with this index is not included into this node.
     */
    public val endIndex: Int,
    public val matcher: Matcher?,
    public val children: Array<ParseNode> = emptyArray()
)
