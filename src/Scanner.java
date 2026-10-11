public class Scanner {
  private byte[] input;
  private int current;

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
      current++;
    }
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
    return new Token(TokenType.NUMBER, n);
  }

  private Token identifier() {
    int start = current;
    while (isAlphaNumeric(peek())) {
      advance();
    }

    String id = new String(input, start, current - start);
    return new Token(TokenType.IDENT, id);
  }

  public Token nextToken() {
    skipWhitespace();

    char ch = peek();
    if (isAlpha(ch)) {
      return identifier();
    }

    if (Character.isDigit(ch)) {
      return number();
    }

    switch (ch) {
      case '\0':
        return new Token(TokenType.EOF, "EOF");
      default:
        throw new Error("lexical error at " + ch);
    }
  }
}