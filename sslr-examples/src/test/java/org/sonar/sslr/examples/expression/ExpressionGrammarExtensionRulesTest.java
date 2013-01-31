/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.sslr.examples.expression;

import org.junit.Test;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class ExpressionGrammarExtensionRulesTest {

  private LexerlessGrammarBuilder base = ExpressionGrammarRules.createGrammarBuilder();
  private LexerlessGrammarBuilder b = ExpressionGrammarExtensionRules.createGrammarBuilder(base);

  /**
   * This test demonstrates how to use {@link org.sonar.sslr.tests.Assertions} to test rules of grammar.
   */
  @Test
  public void rules() {
    assertThat(b.build().rule(ExpressionGrammarRules.EXPRESSION))
        .notMatches("1 + 1")
        .matches("1 plus 1")
        .notMatches("20 * ( 2 + 2 ) - var")
        .matches("20 mul ( 2 plus 2 ) minus var")
        .matches("1 plus fun()");
  }

}
