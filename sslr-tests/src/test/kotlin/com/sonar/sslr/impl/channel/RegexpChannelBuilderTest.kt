/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package com.sonar.sslr.impl.channel

import com.sonar.sslr.impl.channel.RegexpChannelBuilder.anyButNot
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.g
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.o2n
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.one2n
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.opt
import com.sonar.sslr.impl.channel.RegexpChannelBuilder.or
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class RegexpChannelBuilderTest {
    @Test
    fun testOpt() {
        Assert.assertThat(opt("L"), Matchers.equalTo("L?+"))
    }

    @Test
    fun testOne2n() {
        Assert.assertThat(one2n("L"), Matchers.equalTo("L++"))
    }

    @Test
    fun testO2n() {
        Assert.assertThat(o2n("L"), Matchers.equalTo("L*+"))
    }

    @Test
    fun testg() {
        Assert.assertThat(g("L"), Matchers.equalTo("(L)"))
        Assert.assertThat(g("L", "l"), Matchers.equalTo("(Ll)"))
    }

    @Test
    fun testOr() {
        Assert.assertThat(or("L", "l", "U", "u"), Matchers.equalTo("(L|l|U|u)"))
    }

    @Test
    fun testAnyButNot() {
        Assert.assertThat(anyButNot("L", "l"), Matchers.equalTo("[^Ll]"))
    }
}