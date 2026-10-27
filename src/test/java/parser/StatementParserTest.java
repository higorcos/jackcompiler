package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.xml;

import org.junit.jupiter.api.Test;

class StatementParserTest {

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
    assertEquals("line 1, column 11: expected SEMICOLON, found 'EOF'", e.getMessage());
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
}