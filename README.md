# FLR

[![Build Status](https://dev.azure.com/felipebz/z-plsql-analyzer/_apis/build/status/flr?branchName=main)](https://dev.azure.com/felipebz/z-plsql-analyzer/_build/latest?definitionId=13&branchName=main)

FLR is a lightweight Java library which provides everything required to analyse any piece of source code. Using FLR, you can quickly create a lexer, a parser, and some AST visitors to implement quality rules or compute measures.

FLR is a fork of [SonarSource Language Recognizer](https://github.com/SonarSource/sslr/). 

## Features
Here are the main features of FLR:

* Easy integration and use
   * Just add a dependency on a jar file (or several jars, according to what you want to use: lexer/parser, toolkit...)
   * No special step to add to the build process
   * No "untouchable" generated code
* Everything in Java
   * Definition of grammar and lexer directly in code, using Java or Kotlin
   * No break in IDE support (syntax highlighting, code navigation, refactoring, etc)
  
### FLR in action
If you want to start working with FLR, you must be familiar with the following standard concepts: Lexical Analysis, Parsing Expression Grammar and AST (Abstract Syntax Tree). 

FLR also comes with a MiniC language which has been created to easily and simply test all FLR features. This MiniC language can be a good starting point for a beginner to understand how to implement/define the different mandatory layers to analyse a language:

* [Lexer](https://github.com/felipebz/flr/blob/main/flr-testing-harness/src/main/kotlin/com/sonar/sslr/test/minic/MiniCLexer.kt)
* [Grammar](https://github.com/felipebz/flr/blob/main/flr-testing-harness/src/main/kotlin/com/sonar/sslr/test/minic/MiniCGrammar.kt)
* [Parser](https://github.com/felipebz/flr/blob/main/flr-testing-harness/src/main/kotlin/com/sonar/sslr/test/minic/MiniCParser.kt)
* [Toolkit](https://github.com/felipebz/flr/blob/main/flr-testing-harness/src/main/kotlin/com/sonar/sslr/test/minic/MiniCToolkit.kt)
