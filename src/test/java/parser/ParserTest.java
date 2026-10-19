package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ParserTest {

  private static Parser parser(String input) {
    return new Parser(input.getBytes(StandardCharsets.UTF_8));
  }

  private static String xml(String... lines) {
    return String.join("\r\n", lines) + "\r\n";
  }

  @Test
  void termIntegerConstant() {
    Parser p = parser("10;");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <integerConstant> 10 </integerConstant>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termStringConstant() {
    Parser p = parser("\"ola\";");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <stringConstant> ola </stringConstant>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termKeywordConstant() {
    Parser p = parser("true;");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <keyword> true </keyword>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termThis() {
    Parser p = parser("this;");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <keyword> this </keyword>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termVarName() {
    Parser p = parser("x;");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <identifier> x </identifier>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termInvalidThrowsSyntaxException() {
    Parser p = parser(";");
    SyntaxException e = assertThrows(SyntaxException.class, p::parseTerm);
    assertEquals("line 1: expected term, found ';'", e.getMessage());
  }

  @Test
  void expressionWithOperator() {
    Parser p = parser("10+20;");
    p.parseExpression();
    assertEquals(xml(
        "<expression>",
        "  <term>",
        "    <integerConstant> 10 </integerConstant>",
        "  </term>",
        "  <symbol> + </symbol>",
        "  <term>",
        "    <integerConstant> 20 </integerConstant>",
        "  </term>",
        "</expression>"), p.xmlOutput());
  }

  @Test
  void expressionEscapesLessThan() {
    Parser p = parser("a<b;");
    p.parseExpression();
    assertEquals(xml(
        "<expression>",
        "  <term>",
        "    <identifier> a </identifier>",
        "  </term>",
        "  <symbol> &lt; </symbol>",
        "  <term>",
        "    <identifier> b </identifier>",
        "  </term>",
        "</expression>"), p.xmlOutput());
  }

  @Test
  void expressionStringIsNotOperator() {
    Parser p = parser("x \"+\";");
    p.parseExpression();
    assertEquals(xml(
        "<expression>",
        "  <term>",
        "    <identifier> x </identifier>",
        "  </term>",
        "</expression>"), p.xmlOutput());
  }
}