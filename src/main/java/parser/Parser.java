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

  private SyntaxException error(Token token, String message) {
    return new SyntaxException(message + ", found '" + token.lexeme + "'", token.line);
  }

  public String xmlOutput() {
    return xml.toString();
  }
}