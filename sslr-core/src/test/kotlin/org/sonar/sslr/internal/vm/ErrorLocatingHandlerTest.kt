/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.sslr.internal.vm

import org.fest.assertions.Assertions
import org.junit.Test
import org.mockito.Mockito
import org.sonar.sslr.internal.vm.Machine

class ErrorLocatingHandlerTest {
    private val errorLocatingHandler = ErrorLocatingHandler()
    @Test
    fun should_find_location_of_error() {
        val machine = Mockito.mock(Machine::class.java)
        Mockito.`when`(machine.getIndex()).thenReturn(1)
        errorLocatingHandler.onBacktrack(machine)
        Assertions.assertThat(errorLocatingHandler.getErrorIndex()).isEqualTo(1)
        Mockito.`when`(machine.getIndex()).thenReturn(3)
        errorLocatingHandler.onBacktrack(machine)
        Assertions.assertThat(errorLocatingHandler.getErrorIndex()).isEqualTo(3)
        Mockito.`when`(machine.getIndex()).thenReturn(2)
        errorLocatingHandler.onBacktrack(machine)
        Assertions.assertThat(errorLocatingHandler.getErrorIndex()).isEqualTo(3)
    }
}