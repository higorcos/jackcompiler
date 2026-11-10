package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static parser.ParserTestSupport.parser;

import codegen.SymbolTable;
import codegen.SymbolTable.Kind;
import codegen.SymbolTable.Symbol;
import org.junit.jupiter.api.Test;

class SymbolTableParserTest {

  private static final String POINT = """
      class Point {
        field int x, y;
        static int pointCount;

        constructor Point new(int ax, int ay) {
          let x = ax;
          let y = ay;
          let pointCount = pointCount + 1;
          return this;
        }

        method int getx() { return x; }

        method int gety() { return y; }

        method int distance(Point other) {
          var int dx, dy;
          let dx = x - other.getx();
          let dy = y - other.gety();
          return Math.sqrt((dx * dx) + (dy * dy));
        }
      }
      """;

  @Test
  void classVarDecRegistersFieldsAndStatics() {
    Parser p = parser("field int x, y; static int pointCount;");
    p.parseClassVarDec();
    p.parseClassVarDec();
    SymbolTable table = p.symbolTable();
    assertEquals(new Symbol("x", "int", Kind.FIELD, 0), table.resolve("x"));
    assertEquals(new Symbol("y", "int", Kind.FIELD, 1), table.resolve("y"));
    assertEquals(new Symbol("pointCount", "int", Kind.STATIC, 0), table.resolve("pointCount"));
  }

  @Test
  void parameterListRegistersArguments() {
    Parser p = parser("int ax, Point other)");
    p.parseParameterList();
    SymbolTable table = p.symbolTable();
    assertEquals(new Symbol("ax", "int", Kind.ARG, 0), table.resolve("ax"));
    assertEquals(new Symbol("other", "Point", Kind.ARG, 1), table.resolve("other"));
  }

  @Test
  void varDecRegistersLocals() {
    Parser p = parser("var int dx, dy; var boolean done;");
    p.parseVarDec();
    p.parseVarDec();
    SymbolTable table = p.symbolTable();
    assertEquals(new Symbol("dx", "int", Kind.VAR, 0), table.resolve("dx"));
    assertEquals(new Symbol("dy", "int", Kind.VAR, 1), table.resolve("dy"));
    assertEquals(new Symbol("done", "boolean", Kind.VAR, 2), table.resolve("done"));
  }

  @Test
  void methodRegistersThisBeforeParameters() {
    Parser p = parser(POINT);
    p.parse();
    SymbolTable table = p.symbolTable();
    assertEquals(new Symbol("this", "Point", Kind.ARG, 0), table.resolve("this"));
    assertEquals(new Symbol("other", "Point", Kind.ARG, 1), table.resolve("other"));
    assertEquals(new Symbol("dx", "int", Kind.VAR, 0), table.resolve("dx"));
    assertEquals(new Symbol("dy", "int", Kind.VAR, 1), table.resolve("dy"));
    assertEquals(new Symbol("x", "int", Kind.FIELD, 0), table.resolve("x"));
    assertEquals(new Symbol("y", "int", Kind.FIELD, 1), table.resolve("y"));
    assertEquals(new Symbol("pointCount", "int", Kind.STATIC, 0), table.resolve("pointCount"));
  }

  @Test
  void constructorDoesNotRegisterThis() {
    Parser p = parser("class Point { field int x; constructor Point new(int ax) { return this; } }");
    p.parse();
    SymbolTable table = p.symbolTable();
    assertNull(table.resolve("this"));
    assertEquals(new Symbol("ax", "int", Kind.ARG, 0), table.resolve("ax"));
  }

  @Test
  void eachSubroutineStartsNewScope() {
    Parser p = parser("""
        class Main {
          function void a(int n) { var int i; return; }
          function void b() { var int j; return; }
        }
        """);
    p.parse();
    SymbolTable table = p.symbolTable();
    assertNull(table.resolve("n"));
    assertNull(table.resolve("i"));
    assertEquals(new Symbol("j", "int", Kind.VAR, 0), table.resolve("j"));
    assertEquals(0, table.varCount(Kind.ARG));
  }
}