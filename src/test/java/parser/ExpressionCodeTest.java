package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.vm;

import org.junit.jupiter.api.Test;

class ExpressionCodeTest {

  @Test
  void integerConstant() {
    Parser p = parser("10;");
    p.parseExpression();
    assertEquals(vm("push constant 10"), p.vmOutput());
  }

  @Test
  void falseConstant() {
    Parser p = parser("false;");
    p.parseExpression();
    assertEquals(vm("push constant 0"), p.vmOutput());
  }

  @Test
  void nullConstant() {
    Parser p = parser("null;");
    p.parseExpression();
    assertEquals(vm("push constant 0"), p.vmOutput());
  }

  @Test
  void trueConstant() {
    Parser p = parser("true;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 0",
        "not"), p.vmOutput());
  }

  @Test
  void thisKeyword() {
    Parser p = parser("this;");
    p.parseExpression();
    assertEquals(vm("push pointer 0"), p.vmOutput());
  }

  @Test
  void stringConstant() {
    Parser p = parser("\"ola\";");
    p.parseExpression();
    assertEquals(vm(
        "push constant 3",
        "call String.new 1",
        "push constant 111",
        "call String.appendChar 2",
        "push constant 108",
        "call String.appendChar 2",
        "push constant 97",
        "call String.appendChar 2"), p.vmOutput());
  }

  @Test
  void emptyStringConstant() {
    Parser p = parser("\"\";");
    p.parseExpression();
    assertEquals(vm(
        "push constant 0",
        "call String.new 1"), p.vmOutput());
  }
}