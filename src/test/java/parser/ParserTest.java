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

  @Test
  void letSimple() {
    Parser p = parser("let x = 10;");
    p.parseLet();
    assertEquals(xml(
        "<letStatement>",
        "  <keyword> let </keyword>",
        "  <identifier> x </identifier>",
        "  <symbol> = </symbol>",
        "  <expression>",
        "    <term>",
        "      <integerConstant> 10 </integerConstant>",
        "    </term>",
        "  </expression>",
        "  <symbol> ; </symbol>",
        "</letStatement>"), p.xmlOutput());
  }

  @Test
  void letWithArrayIndex() {
    Parser p = parser("let a[1] = 2;");
    p.parseLet();
    assertEquals(xml(
        "<letStatement>",
        "  <keyword> let </keyword>",
        "  <identifier> a </identifier>",
        "  <symbol> [ </symbol>",
        "  <expression>",
        "    <term>",
        "      <integerConstant> 1 </integerConstant>",
        "    </term>",
        "  </expression>",
        "  <symbol> ] </symbol>",
        "  <symbol> = </symbol>",
        "  <expression>",
        "    <term>",
        "      <integerConstant> 2 </integerConstant>",
        "    </term>",
        "  </expression>",
        "  <symbol> ; </symbol>",
        "</letStatement>"), p.xmlOutput());
  }

  @Test
  void letMissingSemicolonThrowsSyntaxException() {
    Parser p = parser("let x = 10");
    SyntaxException e = assertThrows(SyntaxException.class, p::parseLet);
    assertEquals("line 1: expected SEMICOLON, found 'EOF'", e.getMessage());
  }

  @Test
  void doSimpleCall() {
    Parser p = parser("do hello();");
    p.parseDo();
    assertEquals(xml(
        "<doStatement>",
        "  <keyword> do </keyword>",
        "  <identifier> hello </identifier>",
        "  <symbol> ( </symbol>",
        "  <expressionList>",
        "  </expressionList>",
        "  <symbol> ) </symbol>",
        "  <symbol> ; </symbol>",
        "</doStatement>"), p.xmlOutput());
  }

  @Test
  void doQualifiedCall() {
    Parser p = parser("do game.run();");
    p.parseDo();
    assertEquals(xml(
        "<doStatement>",
        "  <keyword> do </keyword>",
        "  <identifier> game </identifier>",
        "  <symbol> . </symbol>",
        "  <identifier> run </identifier>",
        "  <symbol> ( </symbol>",
        "  <expressionList>",
        "  </expressionList>",
        "  <symbol> ) </symbol>",
        "  <symbol> ; </symbol>",
        "</doStatement>"), p.xmlOutput());
  }

  @Test
  void doCallWithArgument() {
    Parser p = parser("do Memory.deAlloc(this);");
    p.parseDo();
    assertEquals(xml(
        "<doStatement>",
        "  <keyword> do </keyword>",
        "  <identifier> Memory </identifier>",
        "  <symbol> . </symbol>",
        "  <identifier> deAlloc </identifier>",
        "  <symbol> ( </symbol>",
        "  <expressionList>",
        "    <expression>",
        "      <term>",
        "        <keyword> this </keyword>",
        "      </term>",
        "    </expression>",
        "  </expressionList>",
        "  <symbol> ) </symbol>",
        "  <symbol> ; </symbol>",
        "</doStatement>"), p.xmlOutput());
  }

  @Test
  void returnWithoutExpression() {
    Parser p = parser("return;");
    p.parseReturn();
    assertEquals(xml(
        "<returnStatement>",
        "  <keyword> return </keyword>",
        "  <symbol> ; </symbol>",
        "</returnStatement>"), p.xmlOutput());
  }

  @Test
  void returnWithExpression() {
    Parser p = parser("return this;");
    p.parseReturn();
    assertEquals(xml(
        "<returnStatement>",
        "  <keyword> return </keyword>",
        "  <expression>",
        "    <term>",
        "      <keyword> this </keyword>",
        "    </term>",
        "  </expression>",
        "  <symbol> ; </symbol>",
        "</returnStatement>"), p.xmlOutput());
  }

  @Test
  void statementsEmpty() {
    Parser p = parser("}");
    p.parseStatements();
    assertEquals(xml(
        "<statements>",
        "</statements>"), p.xmlOutput());
  }

  @Test
  void statementsSequence() {
    Parser p = parser("let x = 1; do f(); return; }");
    p.parseStatements();
    assertEquals(xml(
        "<statements>",
        "  <letStatement>",
        "    <keyword> let </keyword>",
        "    <identifier> x </identifier>",
        "    <symbol> = </symbol>",
        "    <expression>",
        "      <term>",
        "        <integerConstant> 1 </integerConstant>",
        "      </term>",
        "    </expression>",
        "    <symbol> ; </symbol>",
        "  </letStatement>",
        "  <doStatement>",
        "    <keyword> do </keyword>",
        "    <identifier> f </identifier>",
        "    <symbol> ( </symbol>",
        "    <expressionList>",
        "    </expressionList>",
        "    <symbol> ) </symbol>",
        "    <symbol> ; </symbol>",
        "  </doStatement>",
        "  <returnStatement>",
        "    <keyword> return </keyword>",
        "    <symbol> ; </symbol>",
        "  </returnStatement>",
        "</statements>"), p.xmlOutput());
  }

  @Test
  void ifWithoutElse() {
    Parser p = parser("if (x) { return; }");
    p.parseIf();
    assertEquals(xml(
        "<ifStatement>",
        "  <keyword> if </keyword>",
        "  <symbol> ( </symbol>",
        "  <expression>",
        "    <term>",
        "      <identifier> x </identifier>",
        "    </term>",
        "  </expression>",
        "  <symbol> ) </symbol>",
        "  <symbol> { </symbol>",
        "  <statements>",
        "    <returnStatement>",
        "      <keyword> return </keyword>",
        "      <symbol> ; </symbol>",
        "    </returnStatement>",
        "  </statements>",
        "  <symbol> } </symbol>",
        "</ifStatement>"), p.xmlOutput());
  }

  @Test
  void statementsIfWithElse() {
    Parser p = parser("if (false) { } else { return; } }");
    p.parseStatements();
    assertEquals(xml(
        "<statements>",
        "  <ifStatement>",
        "    <keyword> if </keyword>",
        "    <symbol> ( </symbol>",
        "    <expression>",
        "      <term>",
        "        <keyword> false </keyword>",
        "      </term>",
        "    </expression>",
        "    <symbol> ) </symbol>",
        "    <symbol> { </symbol>",
        "    <statements>",
        "    </statements>",
        "    <symbol> } </symbol>",
        "    <keyword> else </keyword>",
        "    <symbol> { </symbol>",
        "    <statements>",
        "      <returnStatement>",
        "        <keyword> return </keyword>",
        "        <symbol> ; </symbol>",
        "      </returnStatement>",
        "    </statements>",
        "    <symbol> } </symbol>",
        "  </ifStatement>",
        "</statements>"), p.xmlOutput());
  }

  @Test
  void whileWithUnaryCondition() {
    Parser p = parser("while (~exit) { do moveSquare(); }");
    p.parseWhile();
    assertEquals(xml(
        "<whileStatement>",
        "  <keyword> while </keyword>",
        "  <symbol> ( </symbol>",
        "  <expression>",
        "    <term>",
        "      <symbol> ~ </symbol>",
        "      <term>",
        "        <identifier> exit </identifier>",
        "      </term>",
        "    </term>",
        "  </expression>",
        "  <symbol> ) </symbol>",
        "  <symbol> { </symbol>",
        "  <statements>",
        "    <doStatement>",
        "      <keyword> do </keyword>",
        "      <identifier> moveSquare </identifier>",
        "      <symbol> ( </symbol>",
        "      <expressionList>",
        "      </expressionList>",
        "      <symbol> ) </symbol>",
        "      <symbol> ; </symbol>",
        "    </doStatement>",
        "  </statements>",
        "  <symbol> } </symbol>",
        "</whileStatement>"), p.xmlOutput());
  }

  @Test
  void statementsWhileEmptyBody() {
    Parser p = parser("while (x) { } }");
    p.parseStatements();
    assertEquals(xml(
        "<statements>",
        "  <whileStatement>",
        "    <keyword> while </keyword>",
        "    <symbol> ( </symbol>",
        "    <expression>",
        "      <term>",
        "        <identifier> x </identifier>",
        "      </term>",
        "    </expression>",
        "    <symbol> ) </symbol>",
        "    <symbol> { </symbol>",
        "    <statements>",
        "    </statements>",
        "    <symbol> } </symbol>",
        "  </whileStatement>",
        "</statements>"), p.xmlOutput());
  }

  @Test
  void classVarDecStatic() {
    Parser p = parser("static boolean test;");
    p.parseClassVarDec();
    assertEquals(xml(
        "<classVarDec>",
        "  <keyword> static </keyword>",
        "  <keyword> boolean </keyword>",
        "  <identifier> test </identifier>",
        "  <symbol> ; </symbol>",
        "</classVarDec>"), p.xmlOutput());
  }

  @Test
  void classVarDecFieldList() {
    Parser p = parser("field int x, y;");
    p.parseClassVarDec();
    assertEquals(xml(
        "<classVarDec>",
        "  <keyword> field </keyword>",
        "  <keyword> int </keyword>",
        "  <identifier> x </identifier>",
        "  <symbol> , </symbol>",
        "  <identifier> y </identifier>",
        "  <symbol> ; </symbol>",
        "</classVarDec>"), p.xmlOutput());
  }

  @Test
  void classVarDecClassType() {
    Parser p = parser("field Square square;");
    p.parseClassVarDec();
    assertEquals(xml(
        "<classVarDec>",
        "  <keyword> field </keyword>",
        "  <identifier> Square </identifier>",
        "  <identifier> square </identifier>",
        "  <symbol> ; </symbol>",
        "</classVarDec>"), p.xmlOutput());
  }
}