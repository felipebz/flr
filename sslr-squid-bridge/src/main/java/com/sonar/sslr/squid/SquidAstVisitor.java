/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.sslr.squid;

import java.util.ArrayList;
import java.util.List;

import org.sonar.squid.api.CodeVisitor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.AstVisitor;
import com.sonar.sslr.api.Grammar;

/**
 * Base class to visit an AST (Abstract Syntactic Tree) generated by a parser.
 * 
 * Methods are visited in the following sequential order : init(), visitFile(), visitNode(), leaveNode(), leaveFile() and destroy()
 */
public class SquidAstVisitor<GRAMMAR extends Grammar> implements CodeVisitor, AstVisitor {

  private final List<AstNodeType> astNodeTypesToVisit = new ArrayList<AstNodeType>();

  /**
   * This method can't be overridden. The method subscribeTo(AstNodeType... astNodeTypes) must be used to while overriding the public void
   * init() method.
   */
  public final List<AstNodeType> getAstNodeTypesToVisit() {
    return astNodeTypesToVisit;
  }

  /**
   * This method must called into the init() method when an AST visitor wants to subscribe to a set of AST node type.
   */
  public final void subscribeTo(AstNodeType... astNodeTypes) {
    for (AstNodeType type : astNodeTypes) {
      astNodeTypesToVisit.add(type);
    }
  }

  /**
   * Initialize the visitor. This is the time to verify that the visitor has everything required to perform it job. This method is called
   * once.
   */
  public void init() {
  }

  /**
   * {@inheritDoc}
   */
  public void visitFile(AstNode astNode) {
  }

  /**
   * {@inheritDoc}
   */
  public void visitNode(AstNode astNode) {
  }

  /**
   * {@inheritDoc}
   */
  public void leaveFile(AstNode astNode) {
  }

  /**
   * {@inheritDoc}
   */
  public void leaveNode(AstNode astNode) {
  }

  /**
   * Destroy the visitor. It is being retired from service.
   */
  public void destroy() {
  }
}