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
}