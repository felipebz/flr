/*
 * Copyright (C) 2010 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonarsource.lexer;

public class Token {

  private TokenType type;
  private String value;
  private int line = 0;
  private int column = 0;
  private String fileName;
  private boolean generatedCode = false;
  
  private boolean copyBook = false;
  private int copyBookOriginalLine = -1;
  private String copyBookOriginalFileName = null;
  

  public Token(TokenType type, String value) {
    this.type = type;
    this.value = value;
  }

  public Token(TokenType type, String value, int line, int column, String fileName) {
    this(type, value);
    this.line = line;
    this.column = column;
    this.fileName = fileName;
  }

  public TokenType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  public String getFileName() {
    return fileName;
  }

  public void setType(TokenType type) {
    this.type = type;
  }

  public boolean isCopyBook() {
    return copyBook;
  }

  public void setCopyBook(boolean copyBook) {
    this.copyBook = copyBook;
  }

  public boolean isGeneratedCode() {
    return generatedCode;
  }

  public void setGeneratedCode(boolean generatedCode) {
    this.generatedCode = generatedCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Token token = (Token) o;

    if (type != null ? type != token.type : token.type != null)
      return false;
    if (value != null ? !value.equals(token.value) : token.value != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = type != null ? type.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return getType() + ": " + getValue();
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setCopyBookOriginalLine(int copyBookOriginalLine) {
    this.copyBookOriginalLine = copyBookOriginalLine;
  }

  public int getCopyBookOriginalLine() {
    return copyBookOriginalLine;
  }

  public void setCopyBookOriginalFileName(String copyBookOriginalFileName) {
    this.copyBookOriginalFileName = copyBookOriginalFileName;
  }

  public String getCopyBookOriginalFileName() {
    return copyBookOriginalFileName;
  }
}
