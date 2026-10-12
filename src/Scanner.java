import java.util.HashMap;
import java.util.Map;

public class Scanner {
  private byte[] input;
  private int current;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("class", TokenType.CLASS);
    keywords.put("constructor", TokenType.CONSTRUCTOR);
    keywords.put("function", TokenType.FUNCTION);
    keywords.put("method", TokenType.METHOD);
    keywords.put("field", TokenType.FIELD);
    keywords.put("static", TokenType.STATIC);
    keywords.put("var", TokenType.VAR);
    keywords.put("int", TokenType.INT);
    keywords.put("char", TokenType.CHAR);
    keywords.put("boolean", TokenType.BOOLEAN);
    keywords.put("void", TokenType.VOID);
    keywords.put("true", TokenType.TRUE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("null", TokenType.NULL);
    keywords.put("this", TokenType.THIS);
    keywords.put("let", TokenType.LET);
    keywords.put("do", TokenType.DO);
    keywords.put("if", TokenType.IF);
    keywords.put("else", TokenType.ELSE);
    keywords.put("while", TokenType.WHILE);
    keywords.put("return", TokenType.RETURN);
  }

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
    TokenType type = keywords.getOrDefault(id, TokenType.IDENT);
    return token(type, id);
  }

  private Token string() {
    advance();
    int start = current;
    while (peek() != '"' && peek() != '\n' && peek() != '\0') {
      advance();
    }

    if (peek() != '"') {
      throw new Error("lexical error: unterminated string");
    }

    String s = new String(input, start, current - start);
    advance();
    return token(TokenType.STRING, s);

  }

  public char peekNext() {
    int next = current + 1;
    return next < input.length ? (char) input[next] : '\0';
  }

  private void skipLineComments() {
    while (peek() != '\n' && peek() != '\0') {
      advance();
    }
  }

  private void skipBlockComments() {
    advance();
    advance();
    while (true) {
      if (peek() == '\0') {
        throw new Error("lexical error: unterminated block comment");
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

    switch (ch) {
      case '+':
        advance();
        return token(TokenType.PLUS, "+");
      case '-':
        advance();
        return token(TokenType.MINUS, "-");
      case '*':
        advance();
        return token(TokenType.ASTERISK, "*");
      case '.':
        advance();
        return token(TokenType.DOT, ".");
      case '&':
        advance();
        return token(TokenType.AND, "&");
      case '|':
        advance();
        return token(TokenType.OR, "|");
      case '~':
        advance();
        return token(TokenType.NOT, "~");
      case '>':
        advance();
        return token(TokenType.GT, ">");
      case '<':
        advance();
        return token(TokenType.LT, "<");
      case '=':
        advance();
        return token(TokenType.EQ, "=");
      case '(':
        advance();
        return token(TokenType.LPAREN, "(");
      case ')':
        advance();
        return token(TokenType.RPAREN, ")");
      case '{':
        advance();
        return token(TokenType.LBRACE, "{");
      case '}':
        advance();
        return token(TokenType.RBRACE, "}");
      case '[':
        advance();
        return token(TokenType.LBRACKET, "[");
      case ']':
        advance();
        return token(TokenType.RBRACKET, "]");
      case ';':
        advance();
        return token(TokenType.SEMICOLON, ";");
      case ',':
        advance();
        return token(TokenType.COMMA, ",");
      case '/':
        advance();
        return token(TokenType.SLASH, "/");
      case '\0':
        return token(TokenType.EOF, "EOF");
      default:
        throw new Error("lexical error at " + ch);
    }
  }
}