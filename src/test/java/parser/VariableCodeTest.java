package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.vm;

import codegen.SymbolTable;
import codegen.SymbolTable.Kind;
import org.junit.jupiter.api.Test;

class VariableCodeTest {

  private static Parser parserWithAllKinds(String input) {
    Parser p = parser(input);
    SymbolTable table = p.symbolTable();
    table.define("s", "int", Kind.STATIC);
    table.define("f", "int", Kind.FIELD);
    table.define("a", "int", Kind.ARG);
    table.define("v", "int", Kind.VAR);
    return p;
  }

  @Test
  void termPushesEachKindFromItsSegment() {
    Parser p = parserWithAllKinds("s + f + a + v;");
    p.parseExpression();
    assertEquals(vm(
        "push static 0",
        "push this 0",
        "add",
        "push argument 0",
        "add",
        "push local 0",
        "add"), p.vmOutput());
  }

  @Test
  void letPopsIntoLocal() {
    Parser p = parserWithAllKinds("let v = 5;");
    p.parseLet();
    assertEquals(vm(
        "push constant 5",
        "pop local 0"), p.vmOutput());
  }

  @Test
  void letPopsIntoField() {
    Parser p = parserWithAllKinds("let f = s;");
    p.parseLet();
    assertEquals(vm(
        "push static 0",
        "pop this 0"), p.vmOutput());
  }

  @Test
  void undeclaredVariableInExpression() {
    Parser p = parser("y;");
    SemanticException e = assertThrows(SemanticException.class, p::parseExpression);
    assertEquals("line 1, column 1: undeclared variable 'y'", e.getMessage());
  }

  @Test
  void undeclaredVariableInLet() {
    Parser p = parser("let y = 1;");
    SemanticException e = assertThrows(SemanticException.class, p::parseLet);
    assertEquals("line 1, column 5: undeclared variable 'y'", e.getMessage());
  }
}