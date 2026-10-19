package parser;

import java.util.Arrays;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;

public class Parser {
  private final Scanner scan;
  private final StringBuilder xml = new StringBuilder();
  private Token currentToken;
  private Token peekToken;
  private int level;

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
      case NUMBER -> expectPeek(TokenType.NUMBER);
      case STRING -> expectPeek(TokenType.STRING);
      case FALSE, NULL, TRUE -> expectPeek(TokenType.FALSE, TokenType.NULL, TokenType.TRUE);
      case THIS -> expectPeek(TokenType.THIS);
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
      expectPeek(peekToken.type);
      parseTerm();
    }
    printNonTerminal("/expression");
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

  private SyntaxException error(Token token, String message) {
    return new SyntaxException(message + ", found '" + token.lexeme + "'", token.line);
  }

  public String xmlOutput() {
    return xml.toString();
  }
}