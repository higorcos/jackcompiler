package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.xml;

import org.junit.jupiter.api.Test;

class ExpressionParserTest {

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
  void termArrayAccess() {
    Parser p = parser("a[2];");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <identifier> a </identifier>",
        "  <symbol> [ </symbol>",
        "  <expression>",
        "    <term>",
        "      <integerConstant> 2 </integerConstant>",
        "    </term>",
        "  </expression>",
        "  <symbol> ] </symbol>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termSubroutineCall() {
    Parser p = parser("Keyboard.keyPressed();");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <identifier> Keyboard </identifier>",
        "  <symbol> . </symbol>",
        "  <identifier> keyPressed </identifier>",
        "  <symbol> ( </symbol>",
        "  <expressionList>",
        "  </expressionList>",
        "  <symbol> ) </symbol>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termSubroutineCallWithArguments() {
    Parser p = parser("Square.new(0, 0, 30);");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <identifier> Square </identifier>",
        "  <symbol> . </symbol>",
        "  <identifier> new </identifier>",
        "  <symbol> ( </symbol>",
        "  <expressionList>",
        "    <expression>",
        "      <term>",
        "        <integerConstant> 0 </integerConstant>",
        "      </term>",
        "    </expression>",
        "    <symbol> , </symbol>",
        "    <expression>",
        "      <term>",
        "        <integerConstant> 0 </integerConstant>",
        "      </term>",
        "    </expression>",
        "    <symbol> , </symbol>",
        "    <expression>",
        "      <term>",
        "        <integerConstant> 30 </integerConstant>",
        "      </term>",
        "    </expression>",
        "  </expressionList>",
        "  <symbol> ) </symbol>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termParenthesizedExpression() {
    Parser p = parser("(x);");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <symbol> ( </symbol>",
        "  <expression>",
        "    <term>",
        "      <identifier> x </identifier>",
        "    </term>",
        "  </expression>",
        "  <symbol> ) </symbol>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termUnaryMinus() {
    Parser p = parser("-j;");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <symbol> - </symbol>",
        "  <term>",
        "    <identifier> j </identifier>",
        "  </term>",
        "</term>"), p.xmlOutput());
  }

  @Test
  void termUnaryNot() {
    Parser p = parser("~exit;");
    p.parseTerm();
    assertEquals(xml(
        "<term>",
        "  <symbol> ~ </symbol>",
        "  <term>",
        "    <identifier> exit </identifier>",
        "  </term>",
        "</term>"), p.xmlOutput());
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