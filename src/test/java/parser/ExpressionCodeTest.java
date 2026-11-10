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
}