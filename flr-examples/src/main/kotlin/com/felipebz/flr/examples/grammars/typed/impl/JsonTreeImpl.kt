//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.examples.grammars.typed.impl

import com.felipebz.flr.examples.grammars.typed.Tree
import com.felipebz.flr.examples.grammars.typed.api.JsonTree

public class JsonTreeImpl(private val arrayOrObject: Tree) : JsonTree {
    override fun arrayOrObject(): Tree {
        return arrayOrObject
    }
}
