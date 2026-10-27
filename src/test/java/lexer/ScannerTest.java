package lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ScannerTest {

  private static Scanner scanner(String input) {
    return new Scanner(input.getBytes(StandardCharsets.UTF_8));
  }

  private static void assertToken(Token tk, TokenType type, String lexeme) {
    assertEquals(type, tk.type);
    assertEquals(lexeme, tk.lexeme);
  }

  @Test
  void keywordsAndIdentifiers() {
    Scanner scan = scanner("class Main");
    assertToken(scan.nextToken(), TokenType.CLASS, "class");
    assertToken(scan.nextToken(), TokenType.IDENT, "Main");
    assertToken(scan.nextToken(), TokenType.EOF, "EOF");
  }

  @Test
  void integerConstant() {
    Scanner scan = scanner("007");
    assertToken(scan.nextToken(), TokenType.NUMBER, "007");
  }

  @Test
  void stringConstantWithoutQuotes() {
    Scanner scan = scanner("\"ola mundo\"");
    assertToken(scan.nextToken(), TokenType.STRING, "ola mundo");
  }

  @Test
  void allSymbols() {
    Scanner scan = scanner("{ } ( ) [ ] . , ; + - * / & | < > = ~");
    TokenType[] expected = {
        TokenType.LBRACE, TokenType.RBRACE, TokenType.LPAREN, TokenType.RPAREN,
        TokenType.LBRACKET, TokenType.RBRACKET, TokenType.DOT, TokenType.COMMA,
        TokenType.SEMICOLON, TokenType.PLUS, TokenType.MINUS, TokenType.ASTERISK,
        TokenType.SLASH, TokenType.AND, TokenType.OR, TokenType.LT, TokenType.GT,
        TokenType.EQ, TokenType.NOT
    };
    for (TokenType type : expected) {
      assertEquals(type, scan.nextToken().type);
    }
    assertEquals(TokenType.EOF, scan.nextToken().type);
  }

  @Test
  void skipsComments() {
    Scanner scan = scanner("// linha\n/* bloco */ /** doc */ x");
    assertToken(scan.nextToken(), TokenType.IDENT, "x");
    assertToken(scan.nextToken(), TokenType.EOF, "EOF");
  }

  @Test
  void tracksLineNumbers() {
    Scanner scan = scanner("a\nb\n\nc");
    assertEquals(1, scan.nextToken().line);
    assertEquals(2, scan.nextToken().line);
    assertEquals(4, scan.nextToken().line);
  }

  @Test
  void tokenEscapesXmlCharacters() {
    assertEquals("<symbol> &lt; </symbol>", new Token(TokenType.LT, "<", 1).toString());
    assertEquals("<symbol> &amp; </symbol>", new Token(TokenType.AND, "&", 1).toString());
  }

  @Test
  void tokenXmlListsAllTokens() {
    String xml = TokenXml.of("let x;".getBytes(StandardCharsets.UTF_8));
    assertEquals(
        "<tokens>\r\n"
            + "<keyword> let </keyword>\r\n"
            + "<identifier> x </identifier>\r\n"
            + "<symbol> ; </symbol>\r\n"
            + "</tokens>\r\n",
        xml);
  }

  @Test
  void unexpectedCharacterThrows() {
    Scanner scan = scanner("@");
    LexicalException e = assertThrows(LexicalException.class, scan::nextToken);
    assertEquals("line 1: unexpected character '@'", e.getMessage());
  }

  @Test
  void unterminatedStringThrows() {
    Scanner scan = scanner("\"abc");
    LexicalException e = assertThrows(LexicalException.class, scan::nextToken);
    assertEquals("line 1: unterminated string", e.getMessage());
  }

  @Test
  void unterminatedBlockCommentThrows() {
    Scanner scan = scanner("x\n/* abc");
    scan.nextToken();
    LexicalException e = assertThrows(LexicalException.class, scan::nextToken);
    assertEquals("line 2: unterminated block comment", e.getMessage());
  }
}