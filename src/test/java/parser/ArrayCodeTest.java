package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        "push constant 2",
        "push local 0",
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
        "push local 1",
        "push constant 1",
        "add",
        "push this 0",
        "add",
        "push local 1",
        "pop temp 0",
        "pop pointer 1",
        "push temp 0",
        "pop that 0"), p.vmOutput());
  }

  @Test
  void readArrayElement() {
    Parser p = parserWithArrays("a[2];");
    p.parseExpression();
    assertEquals(vm(
        "push constant 2",
        "push local 0",
        "add",
        "pop pointer 1",
        "push that 0"), p.vmOutput());
  }

  @Test
  void copyBetweenArrays() {
    Parser p = parserWithArrays("let a[i] = arr[i];");
    p.parseLet();
    assertEquals(vm(
        "push local 1",
        "push local 0",
        "add",
        "push local 1",
        "push this 0",
        "add",
        "pop pointer 1",
        "push that 0",
        "pop temp 0",
        "pop pointer 1",
        "push temp 0",
        "pop that 0"), p.vmOutput());
  }

  @Test
  void undeclaredArray() {
    Parser p = parser("b[1];");
    SemanticException e = assertThrows(SemanticException.class, p::parseExpression);
    assertEquals("line 1, column 1: undeclared variable 'b'", e.getMessage());
  }
}