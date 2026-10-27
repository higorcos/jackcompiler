package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.xml;

import org.junit.jupiter.api.Test;

class StructureParserTest {

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

  @Test
  void parameterListEmpty() {
    Parser p = parser(")");
    p.parseParameterList();
    assertEquals(xml(
        "<parameterList>",
        "</parameterList>"), p.xmlOutput());
  }

  @Test
  void parameterListMultiple() {
    Parser p = parser("int Ax, int Ay, int Asize)");
    p.parseParameterList();
    assertEquals(xml(
        "<parameterList>",
        "  <keyword> int </keyword>",
        "  <identifier> Ax </identifier>",
        "  <symbol> , </symbol>",
        "  <keyword> int </keyword>",
        "  <identifier> Ay </identifier>",
        "  <symbol> , </symbol>",
        "  <keyword> int </keyword>",
        "  <identifier> Asize </identifier>",
        "</parameterList>"), p.xmlOutput());
  }

  @Test
  void varDecList() {
    Parser p = parser("var int i, j;");
    p.parseVarDec();
    assertEquals(xml(
        "<varDec>",
        "  <keyword> var </keyword>",
        "  <keyword> int </keyword>",
        "  <identifier> i </identifier>",
        "  <symbol> , </symbol>",
        "  <identifier> j </identifier>",
        "  <symbol> ; </symbol>",
        "</varDec>"), p.xmlOutput());
  }

  @Test
  void varDecClassType() {
    Parser p = parser("var SquareGame game;");
    p.parseVarDec();
    assertEquals(xml(
        "<varDec>",
        "  <keyword> var </keyword>",
        "  <identifier> SquareGame </identifier>",
        "  <identifier> game </identifier>",
        "  <symbol> ; </symbol>",
        "</varDec>"), p.xmlOutput());
  }

  @Test
  void subroutineConstructorWithParameter() {
    Parser p = parser("constructor Square new(int Ax) { return this; }");
    p.parseSubroutine();
    assertEquals(xml(
        "<subroutineDec>",
        "  <keyword> constructor </keyword>",
        "  <identifier> Square </identifier>",
        "  <identifier> new </identifier>",
        "  <symbol> ( </symbol>",
        "  <parameterList>",
        "    <keyword> int </keyword>",
        "    <identifier> Ax </identifier>",
        "  </parameterList>",
        "  <symbol> ) </symbol>",
        "  <subroutineBody>",
        "    <symbol> { </symbol>",
        "    <statements>",
        "      <returnStatement>",
        "        <keyword> return </keyword>",
        "        <expression>",
        "          <term>",
        "            <keyword> this </keyword>",
        "          </term>",
        "        </expression>",
        "        <symbol> ; </symbol>",
        "      </returnStatement>",
        "    </statements>",
        "    <symbol> } </symbol>",
        "  </subroutineBody>",
        "</subroutineDec>"), p.xmlOutput());
  }

  @Test
  void subroutineFunctionVoidWithVarDecs() {
    Parser p = parser("function void main() { var int i; var char c; return; }");
    p.parseSubroutine();
    assertEquals(xml(
        "<subroutineDec>",
        "  <keyword> function </keyword>",
        "  <keyword> void </keyword>",
        "  <identifier> main </identifier>",
        "  <symbol> ( </symbol>",
        "  <parameterList>",
        "  </parameterList>",
        "  <symbol> ) </symbol>",
        "  <subroutineBody>",
        "    <symbol> { </symbol>",
        "    <varDec>",
        "      <keyword> var </keyword>",
        "      <keyword> int </keyword>",
        "      <identifier> i </identifier>",
        "      <symbol> ; </symbol>",
        "    </varDec>",
        "    <varDec>",
        "      <keyword> var </keyword>",
        "      <keyword> char </keyword>",
        "      <identifier> c </identifier>",
        "      <symbol> ; </symbol>",
        "    </varDec>",
        "    <statements>",
        "      <returnStatement>",
        "        <keyword> return </keyword>",
        "        <symbol> ; </symbol>",
        "      </returnStatement>",
        "    </statements>",
        "    <symbol> } </symbol>",
        "  </subroutineBody>",
        "</subroutineDec>"), p.xmlOutput());
  }

  @Test
  void classEmpty() {
    Parser p = parser("class Main { }");
    p.parseClass();
    assertEquals(xml(
        "<class>",
        "  <keyword> class </keyword>",
        "  <identifier> Main </identifier>",
        "  <symbol> { </symbol>",
        "  <symbol> } </symbol>",
        "</class>"), p.xmlOutput());
  }

  @Test
  void parseClassWithVarAndSubroutine() {
    Parser p = parser("class A { static int x; function void f() { return; } }");
    p.parse();
    assertEquals(xml(
        "<class>",
        "  <keyword> class </keyword>",
        "  <identifier> A </identifier>",
        "  <symbol> { </symbol>",
        "  <classVarDec>",
        "    <keyword> static </keyword>",
        "    <keyword> int </keyword>",
        "    <identifier> x </identifier>",
        "    <symbol> ; </symbol>",
        "  </classVarDec>",
        "  <subroutineDec>",
        "    <keyword> function </keyword>",
        "    <keyword> void </keyword>",
        "    <identifier> f </identifier>",
        "    <symbol> ( </symbol>",
        "    <parameterList>",
        "    </parameterList>",
        "    <symbol> ) </symbol>",
        "    <subroutineBody>",
        "      <symbol> { </symbol>",
        "      <statements>",
        "        <returnStatement>",
        "          <keyword> return </keyword>",
        "          <symbol> ; </symbol>",
        "        </returnStatement>",
        "      </statements>",
        "      <symbol> } </symbol>",
        "    </subroutineBody>",
        "  </subroutineDec>",
        "  <symbol> } </symbol>",
        "</class>"), p.xmlOutput());
  }

  @Test
  void parseRejectsTrailingTokens() {
    Parser p = parser("class A { } x");
    SyntaxException e = assertThrows(SyntaxException.class, p::parse);
    assertEquals("line 1, column 13: expected end of file, found 'x'", e.getMessage());
  }
}