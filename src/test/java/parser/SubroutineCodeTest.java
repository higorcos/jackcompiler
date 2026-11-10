package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static parser.ParserTestSupport.parser;
import static parser.ParserTestSupport.vm;

import org.junit.jupiter.api.Test;

class SubroutineCodeTest {

  @Test
  void simpleFunctions() {
    Parser p = parser("""
        class Main {
          function int soma(int x, int y) { return 30; }
          function void main() { var int d; return; }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Main.soma 0",
        "push constant 30",
        "return",
        "function Main.main 1",
        "push constant 0",
        "return"), p.vmOutput());
  }

  @Test
  void raizFromBook() {
    Parser p = parser("""
        class Main {
          function int raiz(int x) {
            var int low, high, mid;
            let low = 0;
            let high = x + 1;
            while ((high - low) > 1) {
              let mid = (high + low) / 2;
              if ((mid * mid) > x) {
                let high = mid;
              } else {
                let low = mid;
              }
            }
            return low;
          }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Main.raiz 3",
        "push constant 0",
        "pop local 0",
        "push argument 0",
        "push constant 1",
        "add",
        "pop local 1",
        "label WHILE_EXP0",
        "push local 1",
        "push local 0",
        "sub",
        "push constant 1",
        "gt",
        "not",
        "if-goto WHILE_END0",
        "push local 1",
        "push local 0",
        "add",
        "push constant 2",
        "call Math.divide 2",
        "pop local 2",
        "push local 2",
        "push local 2",
        "call Math.multiply 2",
        "push argument 0",
        "gt",
        "if-goto IF_TRUE0",
        "goto IF_FALSE0",
        "label IF_TRUE0",
        "push local 2",
        "pop local 1",
        "goto IF_END0",
        "label IF_FALSE0",
        "push local 2",
        "pop local 0",
        "label IF_END0",
        "goto WHILE_EXP0",
        "label WHILE_END0",
        "push local 0",
        "return"), p.vmOutput());
  }
}