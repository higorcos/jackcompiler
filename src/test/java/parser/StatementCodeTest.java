package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.vm;

import org.junit.jupiter.api.Test;

class StatementCodeTest {

  @Test
  void returnVoidPushesZero() {
    Parser p = parser("return;");
    p.parseReturn();
    assertEquals(vm(
        "push constant 0",
        "return"), p.vmOutput());
  }

  @Test
  void returnWithExpression() {
    Parser p = parser("return 1 + 2;");
    p.parseReturn();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "add",
        "return"), p.vmOutput());
  }
}