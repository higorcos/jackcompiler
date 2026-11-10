package parser;

import codegen.SymbolTable;
import codegen.SymbolTable.Kind;
import codegen.VMWriter;
import codegen.VMWriter.Command;
import codegen.VMWriter.Segment;
import java.util.Arrays;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;

public class Parser {
  private final Scanner scan;
  private final StringBuilder xml = new StringBuilder();
  private final SymbolTable symbolTable = new SymbolTable();
  private final VMWriter vmWriter = new VMWriter();
  private Token currentToken;
  private Token peekToken;
  private int level;
  private String className;

  public Parser(byte[] input) {
    scan = new Scanner(input);
    nextToken();
  }

  private void nextToken() {
    currentToken = peekToken;
    peekToken = scan.nextToken();
  }

  boolean peekTokenIs(TokenType type) {
    return peekToken.type == type;
  }

  private void writeLine(String line) {
    xml.append("  ".repeat(level)).append(line).append("\r\n");
  }

  private void expectPeek(TokenType type) {
    if (peekToken.type == type) {
      nextToken();
      writeLine(currentToken.toString());
    } else {
      throw error(peekToken, "expected " + type.name());
    }
  }

  private void expectPeek(TokenType... types) {
    for (TokenType type : types) {
      if (peekToken.type == type) {
        expectPeek(type);
        return;
      }
    }
    throw error(peekToken, "expected one of " + Arrays.toString(types));
  }

  void printNonTerminal(String name) {
    boolean closing = name.startsWith("/");
    if (closing) {
      level--;
    }
    writeLine("<" + name + ">");
    if (!closing) {
      level++;
    }
  }

  void parseTerm() {
    printNonTerminal("term");
    switch (peekToken.type) {
      case NUMBER -> {
        expectPeek(TokenType.NUMBER);
        vmWriter.writePush(Segment.CONST, Integer.parseInt(currentToken.lexeme));
      }
      case STRING -> {
        expectPeek(TokenType.STRING);
        String text = currentToken.lexeme;
        vmWriter.writePush(Segment.CONST, text.length());
        vmWriter.writeCall("String.new", 1);
        for (int i = 0; i < text.length(); i++) {
          vmWriter.writePush(Segment.CONST, text.charAt(i));
          vmWriter.writeCall("String.appendChar", 2);
        }
      }
      case FALSE, NULL, TRUE -> {
        expectPeek(TokenType.FALSE, TokenType.NULL, TokenType.TRUE);
        vmWriter.writePush(Segment.CONST, 0);
        if (currentToken.type == TokenType.TRUE) {
          vmWriter.writeArithmetic(Command.NOT);
        }
      }
      case THIS -> {
        expectPeek(TokenType.THIS);
        vmWriter.writePush(Segment.POINTER, 0);
      }
      case IDENT -> {
        expectPeek(TokenType.IDENT);
        if (peekTokenIs(TokenType.LBRACKET)) {
          expectPeek(TokenType.LBRACKET);
          parseExpression();
          expectPeek(TokenType.RBRACKET);
        } else if (peekTokenIs(TokenType.LPAREN) || peekTokenIs(TokenType.DOT)) {
          parseSubroutineCall();
        }
      }
      case LPAREN -> {
        expectPeek(TokenType.LPAREN);
        parseExpression();
        expectPeek(TokenType.RPAREN);
      }
      case MINUS, NOT -> {
        expectPeek(TokenType.MINUS, TokenType.NOT);
        parseTerm();
      }
      default -> throw error(peekToken, "expected term");
    }
    printNonTerminal("/term");
  }

  private static boolean isOperator(TokenType type) {
    return switch (type) {
      case PLUS, MINUS, ASTERISK, SLASH, AND, OR, LT, GT, EQ -> true;
      default -> false;
    };
  }

  void parseExpression() {
    printNonTerminal("expression");
    parseTerm();
    while (isOperator(peekToken.type)) {
      TokenType op = peekToken.type;
      expectPeek(op);
      parseTerm();
      compileOperator(op);
    }
    printNonTerminal("/expression");
  }

  private void compileOperator(TokenType op) {
    switch (op) {
      case ASTERISK -> vmWriter.writeCall("Math.multiply", 2);
      case SLASH -> vmWriter.writeCall("Math.divide", 2);
      case PLUS -> vmWriter.writeArithmetic(Command.ADD);
      case MINUS -> vmWriter.writeArithmetic(Command.SUB);
      case LT -> vmWriter.writeArithmetic(Command.LT);
      case GT -> vmWriter.writeArithmetic(Command.GT);
      case EQ -> vmWriter.writeArithmetic(Command.EQ);
      case AND -> vmWriter.writeArithmetic(Command.AND);
      case OR -> vmWriter.writeArithmetic(Command.OR);
      default -> throw error(currentToken, "expected operator");
    }
  }

  void parseExpressionList() {
    printNonTerminal("expressionList");
    if (!peekTokenIs(TokenType.RPAREN)) {
      parseExpression();
      while (peekTokenIs(TokenType.COMMA)) {
        expectPeek(TokenType.COMMA);
        parseExpression();
      }
    }
    printNonTerminal("/expressionList");
  }

  void parseLet() {
    printNonTerminal("letStatement");
    expectPeek(TokenType.LET);
    expectPeek(TokenType.IDENT);

    if (peekTokenIs(TokenType.LBRACKET)) {
      expectPeek(TokenType.LBRACKET);
      parseExpression();
      expectPeek(TokenType.RBRACKET);
    }

    expectPeek(TokenType.EQ);
    parseExpression();
    expectPeek(TokenType.SEMICOLON);
    printNonTerminal("/letStatement");
  }

  void parseSubroutineCall() {
    if (peekTokenIs(TokenType.DOT)) {
      expectPeek(TokenType.DOT);
      expectPeek(TokenType.IDENT);
    }

    expectPeek(TokenType.LPAREN);
    parseExpressionList();
    expectPeek(TokenType.RPAREN);
  }

  void parseDo() {
    printNonTerminal("doStatement");
    expectPeek(TokenType.DO);
    expectPeek(TokenType.IDENT);
    parseSubroutineCall();
    expectPeek(TokenType.SEMICOLON);
    printNonTerminal("/doStatement");
  }

  void parseReturn() {
    printNonTerminal("returnStatement");
    expectPeek(TokenType.RETURN);
    if (!peekTokenIs(TokenType.SEMICOLON)) {
      parseExpression();
    }
    expectPeek(TokenType.SEMICOLON);
    printNonTerminal("/returnStatement");
  }

  void parseIf() {
    printNonTerminal("ifStatement");
    expectPeek(TokenType.IF);
    expectPeek(TokenType.LPAREN);
    parseExpression();
    expectPeek(TokenType.RPAREN);
    expectPeek(TokenType.LBRACE);
    parseStatements();
    expectPeek(TokenType.RBRACE);

    if (peekTokenIs(TokenType.ELSE)) {
      expectPeek(TokenType.ELSE);
      expectPeek(TokenType.LBRACE);
      parseStatements();
      expectPeek(TokenType.RBRACE);
    }

    printNonTerminal("/ifStatement");
  }

  void parseWhile() {
    printNonTerminal("whileStatement");
    expectPeek(TokenType.WHILE);
    expectPeek(TokenType.LPAREN);
    parseExpression();
    expectPeek(TokenType.RPAREN);
    expectPeek(TokenType.LBRACE);
    parseStatements();
    expectPeek(TokenType.RBRACE);
    printNonTerminal("/whileStatement");
  }

  private static boolean isStatement(TokenType type) {
    return switch (type) {
      case LET, DO, RETURN, IF, WHILE -> true;
      default -> false;
    };
  }

  void parseStatements() {
    printNonTerminal("statements");
    while (isStatement(peekToken.type)) {
      parseStatement();
    }
    printNonTerminal("/statements");
  }

  private void parseStatement() {
    switch (peekToken.type) {
      case LET -> parseLet();
      case DO -> parseDo();
      case RETURN -> parseReturn();
      case IF -> parseIf();
      case WHILE -> parseWhile();
      default -> throw error(peekToken, "expected statement");
    }
  }

  void parseClassVarDec() {
    printNonTerminal("classVarDec");
    expectPeek(TokenType.STATIC, TokenType.FIELD);
    Kind kind = currentToken.type == TokenType.STATIC ? Kind.STATIC : Kind.FIELD;
    parseType();
    String type = currentToken.lexeme;
    expectPeek(TokenType.IDENT);
    symbolTable.define(currentToken.lexeme, type, kind);
    while (peekTokenIs(TokenType.COMMA)) {
      expectPeek(TokenType.COMMA);
      expectPeek(TokenType.IDENT);
      symbolTable.define(currentToken.lexeme, type, kind);
    }
    expectPeek(TokenType.SEMICOLON);
    printNonTerminal("/classVarDec");
  }

  private void parseType() {
    expectPeek(TokenType.INT, TokenType.CHAR, TokenType.BOOLEAN, TokenType.IDENT);
  }

  void parseParameterList() {
    printNonTerminal("parameterList");
    if (!peekTokenIs(TokenType.RPAREN)) {
      parseType();
      String type = currentToken.lexeme;
      expectPeek(TokenType.IDENT);
      symbolTable.define(currentToken.lexeme, type, Kind.ARG);
      while (peekTokenIs(TokenType.COMMA)) {
        expectPeek(TokenType.COMMA);
        parseType();
        type = currentToken.lexeme;
        expectPeek(TokenType.IDENT);
        symbolTable.define(currentToken.lexeme, type, Kind.ARG);
      }
    }
    printNonTerminal("/parameterList");
  }

  void parseVarDec() {
    printNonTerminal("varDec");
    expectPeek(TokenType.VAR);
    parseType();
    String type = currentToken.lexeme;
    expectPeek(TokenType.IDENT);
    symbolTable.define(currentToken.lexeme, type, Kind.VAR);
    while (peekTokenIs(TokenType.COMMA)) {
      expectPeek(TokenType.COMMA);
      expectPeek(TokenType.IDENT);
      symbolTable.define(currentToken.lexeme, type, Kind.VAR);
    }
    expectPeek(TokenType.SEMICOLON);
    printNonTerminal("/varDec");
  }

  void parseSubroutine() {
    printNonTerminal("subroutineDec");
    symbolTable.startSubroutine();
    expectPeek(TokenType.CONSTRUCTOR, TokenType.FUNCTION, TokenType.METHOD);
    if (currentToken.type == TokenType.METHOD) {
      symbolTable.define("this", className, Kind.ARG);
    }
    if (peekTokenIs(TokenType.VOID)) {
      expectPeek(TokenType.VOID);
    } else {
      parseType();
    }
    expectPeek(TokenType.IDENT);
    expectPeek(TokenType.LPAREN);
    parseParameterList();
    expectPeek(TokenType.RPAREN);
    parseSubroutineBody();
    printNonTerminal("/subroutineDec");
  }

  void parseSubroutineBody() {
    printNonTerminal("subroutineBody");
    expectPeek(TokenType.LBRACE);
    while (peekTokenIs(TokenType.VAR)) {
      parseVarDec();
    }
    parseStatements();
    expectPeek(TokenType.RBRACE);
    printNonTerminal("/subroutineBody");
  }

  public void parse() {
    parseClass();
    if (!peekTokenIs(TokenType.EOF)) {
      throw error(peekToken, "expected end of file");
    }
  }

  void parseClass() {
    printNonTerminal("class");
    expectPeek(TokenType.CLASS);
    expectPeek(TokenType.IDENT);
    className = currentToken.lexeme;
    expectPeek(TokenType.LBRACE);

    while (peekTokenIs(TokenType.STATIC) || peekTokenIs(TokenType.FIELD)) {
      parseClassVarDec();
    }

    while (peekTokenIs(TokenType.CONSTRUCTOR) || peekTokenIs(TokenType.FUNCTION)
        || peekTokenIs(TokenType.METHOD)) {
      parseSubroutine();
    }

    expectPeek(TokenType.RBRACE);
    printNonTerminal("/class");
  }

  private SyntaxException error(Token token, String message) {
    return new SyntaxException(message + ", found '" + token.lexeme + "'", token.line, token.column);
  }

  SymbolTable symbolTable() {
    return symbolTable;
  }

  public String xmlOutput() {
    return xml.toString();
  }

  public String vmOutput() {
    return vmWriter.output();
  }
}