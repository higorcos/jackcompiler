package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.vm;

import codegen.SymbolTable;
import codegen.SymbolTable.Kind;
import org.junit.jupiter.api.Test;

class ArrayCodeTest {

  private static Parser parserWithArrays(String input) {
    Parser p = parser(input);
    SymbolTable table = p.symbolTable();
    table.define("arr", "Array", Kind.FIELD);
    table.define("a", "Array", Kind.VAR);
    table.define("i", "int", Kind.VAR);
    return p;
  }

  @Test
  void letArrayElement() {
    Parser p = parserWithArrays("let a[2] = 5;");
    p.parseLet();
    assertEquals(vm(
        "push local 0",
        "push constant 2",
        "add",
        "push constant 5",
        "pop temp 0",
        "pop pointer 1",
        "push temp 0",
        "pop that 0"), p.vmOutput());
  }

  @Test
  void letArrayElementOnFieldWithVariableIndex() {
    Parser p = parserWithArrays("let arr[i + 1] = i;");
    p.parseLet();
    assertEquals(vm(
        "push this 0",
        "push local 1",
        "push constant 1",
        "add",
        "add",
        "push local 1",
        "pop temp 0",
        "pop pointer 1",
        "push temp 0",
        "pop that 0"), p.vmOutput());
  }
}