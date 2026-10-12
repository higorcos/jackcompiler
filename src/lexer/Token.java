package lexer;

public class Token {
  public final TokenType type;
  public String lexeme;
  public final int line;

  public Token(TokenType type, String lexeme, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.line = line;
  }

  private String category() {
    return switch (type) {
      case NUMBER -> "integerConstant";
      case IDENT -> "identifier";
      case STRING -> "stringConstant";
      case LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,
          COMMA, SEMICOLON, DOT, PLUS, MINUS, ASTERISK, SLASH,
          AND, OR, NOT, LT, GT, EQ ->
        "symbol";
      default -> "keyword";

    };
  }

  private String escape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  public String toString() {
    String c = category();
    return "<" + c + "> " + escape(lexeme) + " </" + c + ">";
  }
}
