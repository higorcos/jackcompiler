package lexer;

public class Scanner {
  private final byte[] input;
  private int current;
  private int line = 1;

  public Scanner(byte[] input) {
    this.input = input;
  }

  private char peek() {
    if (current < input.length) {
      return (char) input[current];
    }
    return '\0';
  }

  private void advance() {
    char ch = peek();
    if (ch != '\0') {
      if (ch == '\n') {
        line++;
      }
      current++;
    }
  }

  private Token token(TokenType type, String lexeme) {
    return new Token(type, lexeme, line);
  }

  private boolean isAlpha(char ch) {
    return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
  }

  private boolean isAlphaNumeric(char ch) {
    return isAlpha(ch) || Character.isDigit(ch);
  }

  private void skipWhitespace() {
    char ch = peek();
    while (ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n') {
      advance();
      ch = peek();
    }
  }

  private Token number() {
    int start = current;
    while (Character.isDigit(peek())) {
      advance();
    }

    String n = new String(input, start, current - start);
    return token(TokenType.NUMBER, n);
  }

  private Token identifier() {
    int start = current;
    while (isAlphaNumeric(peek())) {
      advance();
    }

    String id = new String(input, start, current - start);
    TokenType type = Lexicon.wordType(id);
    return token(type, id);
  }

  private Token string() {
    advance();
    int start = current;
    while (peek() != '"' && peek() != '\n' && peek() != '\0') {
      advance();
    }

    if (peek() != '"') {
      throw new LexicalException("unterminated string", line);
    }

    String s = new String(input, start, current - start);
    advance();
    return token(TokenType.STRING, s);
  }

  private char peekNext() {
    int next = current + 1;
    return next < input.length ? (char) input[next] : '\0';
  }

  private void skipLineComments() {
    while (peek() != '\n' && peek() != '\0') {
      advance();
    }
  }

  private void skipBlockComments() {
    int startLine = line;
    advance();
    advance();
    while (true) {
      if (peek() == '\0') {
        throw new LexicalException("unterminated block comment", startLine);
      }
      if (peek() == '*' && peekNext() == '/') {
        advance();
        advance();
        return;
      }

      advance();
    }
  }

  private void skipIgnored() {
    while (true) {
      skipWhitespace();
      if (peek() == '/' && peekNext() == '/') {
        skipLineComments();
      } else if (peek() == '/' && peekNext() == '*') {
        skipBlockComments();
      } else {
        return;
      }
    }
  }

  public Token nextToken() {
    skipIgnored();

    char ch = peek();
    if (isAlpha(ch)) {
      return identifier();
    }

    if (Character.isDigit(ch)) {
      return number();
    }

    if (ch == '"') {
      return string();
    }

    if (ch == '\0') {
      return token(TokenType.EOF, "EOF");
    }

    TokenType type = Lexicon.symbolType(ch);
    if (type == null) {
      throw new LexicalException("unexpected character '" + ch + "'", line);
    }

    advance();
    return token(type, String.valueOf(ch));
  }
}