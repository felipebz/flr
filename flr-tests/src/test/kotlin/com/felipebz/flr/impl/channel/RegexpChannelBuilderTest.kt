//
// SPDX-FileCopyrightText: 2010-2021 SonarSource SA
// SPDX-FileCopyrightText: 2021 Felipe Zorzo
// SPDX-License-Identifier: LGPL-3.0-or-later
//

package com.felipebz.flr.impl.channel

import com.felipebz.flr.impl.channel.RegexpChannelBuilder.anyButNot
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.g
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.o2n
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.one2n
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.opt
import com.felipebz.flr.impl.channel.RegexpChannelBuilder.or
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegexpChannelBuilderTest {
    @Test
    fun testOpt() {
        assertEquals(opt("L"), "L?+")
    }

    @Test
    fun testOne2n() {
         assertEquals(one2n("L"), "L++")
    }

    @Test
    fun testO2n() {
         assertEquals(o2n("L"), "L*+")
    }

    @Test
    fun testg() {
         assertEquals(g("L"), "(L)")
         assertEquals(g("L", "l"), "(Ll)")
    }

    @Test
    fun testOr() {
         assertEquals(or("L", "l", "U", "u"), "(L|l|U|u)")
    }

    @Test
    fun testAnyButNot() {
         assertEquals(anyButNot("L", "l"), "[^Ll]")
    }
}
