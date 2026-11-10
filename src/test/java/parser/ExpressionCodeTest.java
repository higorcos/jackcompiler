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

  @Test
  void binaryPlus() {
    Parser p = parser("1 + 2;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "add"), p.vmOutput());
  }

  @Test
  void binaryOperatorsAreLeftToRight() {
    Parser p = parser("1 + 2 * 3;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "add",
        "push constant 3",
        "call Math.multiply 2"), p.vmOutput());
  }

  @Test
  void multiplyAndDivideCallMath() {
    Parser p = parser("6 * 2 / 3;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 6",
        "push constant 2",
        "call Math.multiply 2",
        "push constant 3",
        "call Math.divide 2"), p.vmOutput());
  }

  @Test
  void comparisonAndLogicOperators() {
    Parser p = parser("1 - 2 & 3 | 4 < 5 > 6 = 7;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "sub",
        "push constant 3",
        "and",
        "push constant 4",
        "or",
        "push constant 5",
        "lt",
        "push constant 6",
        "gt",
        "push constant 7",
        "eq"), p.vmOutput());
  }

  @Test
  void unaryMinus() {
    Parser p = parser("-5;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 5",
        "neg"), p.vmOutput());
  }

  @Test
  void unaryNot() {
    Parser p = parser("~0;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 0",
        "not"), p.vmOutput());
  }

  @Test
  void binaryAndUnaryMinus() {
    Parser p = parser("1 - -2;");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "neg",
        "sub"), p.vmOutput());
  }

  @Test
  void parenthesesAreCompiledFirst() {
    Parser p = parser("1 + (2 * 3);");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "push constant 3",
        "call Math.multiply 2",
        "add"), p.vmOutput());
  }
}