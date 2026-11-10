package codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import codegen.SymbolTable.Kind;
import codegen.SymbolTable.Symbol;
import org.junit.jupiter.api.Test;

class SymbolTableTest {

  private static SymbolTable pointClass() {
    SymbolTable table = new SymbolTable();
    table.define("x", "int", Kind.FIELD);
    table.define("y", "int", Kind.FIELD);
    table.define("pointCount", "int", Kind.STATIC);
    return table;
  }

  @Test
  void classScopeCountsStaticAndFieldSeparately() {
    SymbolTable table = pointClass();
    assertEquals(new Symbol("x", "int", Kind.FIELD, 0), table.resolve("x"));
    assertEquals(new Symbol("y", "int", Kind.FIELD, 1), table.resolve("y"));
    assertEquals(new Symbol("pointCount", "int", Kind.STATIC, 0), table.resolve("pointCount"));
    assertEquals(2, table.varCount(Kind.FIELD));
    assertEquals(1, table.varCount(Kind.STATIC));
  }

  @Test
  void constructorArgumentsStartAtZero() {
    SymbolTable table = pointClass();
    table.startSubroutine();
    table.define("ax", "int", Kind.ARG);
    table.define("ay", "int", Kind.ARG);
    assertEquals(new Symbol("ax", "int", Kind.ARG, 0), table.resolve("ax"));
    assertEquals(new Symbol("ay", "int", Kind.ARG, 1), table.resolve("ay"));
  }

  @Test
  void methodReceivesThisAsFirstArgument() {
    SymbolTable table = pointClass();
    table.startSubroutine();
    table.define("this", "Point", Kind.ARG);
    table.define("other", "Point", Kind.ARG);
    table.define("dx", "int", Kind.VAR);
    table.define("dy", "int", Kind.VAR);
    assertEquals(new Symbol("this", "Point", Kind.ARG, 0), table.resolve("this"));
    assertEquals(new Symbol("other", "Point", Kind.ARG, 1), table.resolve("other"));
    assertEquals(new Symbol("dx", "int", Kind.VAR, 0), table.resolve("dx"));
    assertEquals(new Symbol("dy", "int", Kind.VAR, 1), table.resolve("dy"));
    assertEquals(2, table.varCount(Kind.VAR));
  }

  @Test
  void startSubroutineClearsOnlySubroutineScope() {
    SymbolTable table = pointClass();
    table.startSubroutine();
    table.define("ax", "int", Kind.ARG);
    table.define("dx", "int", Kind.VAR);
    table.startSubroutine();
    assertNull(table.resolve("ax"));
    assertNull(table.resolve("dx"));
    assertEquals(0, table.varCount(Kind.ARG));
    assertEquals(0, table.varCount(Kind.VAR));
    assertEquals(new Symbol("x", "int", Kind.FIELD, 0), table.resolve("x"));
    assertEquals(2, table.varCount(Kind.FIELD));
  }

  @Test
  void resolvePrefersSubroutineScope() {
    SymbolTable table = pointClass();
    table.startSubroutine();
    table.define("x", "boolean", Kind.VAR);
    assertEquals(new Symbol("x", "boolean", Kind.VAR, 0), table.resolve("x"));
  }

  @Test
  void resolveUnknownReturnsNull() {
    assertNull(pointClass().resolve("z"));
  }
}