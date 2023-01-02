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