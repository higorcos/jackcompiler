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

  @Test
  void ifWithoutElse() {
    Parser p = parser("if (true) { return 1; }");
    p.parseIf();
    assertEquals(vm(
        "push constant 0",
        "not",
        "if-goto IF_TRUE0",
        "goto IF_FALSE0",
        "label IF_TRUE0",
        "push constant 1",
        "return",
        "label IF_FALSE0"), p.vmOutput());
  }

  @Test
  void ifWithElse() {
    Parser p = parser("if (false) { return 1; } else { return 2; }");
    p.parseIf();
    assertEquals(vm(
        "push constant 0",
        "if-goto IF_TRUE0",
        "goto IF_FALSE0",
        "label IF_TRUE0",
        "push constant 1",
        "return",
        "goto IF_END0",
        "label IF_FALSE0",
        "push constant 2",
        "return",
        "label IF_END0"), p.vmOutput());
  }

  @Test
  void nestedIfsGetUniqueLabels() {
    Parser p = parser("if (true) { if (false) { return 1; } } else { return 2; }");
    p.parseIf();
    assertEquals(vm(
        "push constant 0",
        "not",
        "if-goto IF_TRUE0",
        "goto IF_FALSE0",
        "label IF_TRUE0",
        "push constant 0",
        "if-goto IF_TRUE1",
        "goto IF_FALSE1",
        "label IF_TRUE1",
        "push constant 1",
        "return",
        "label IF_FALSE1",
        "goto IF_END0",
        "label IF_FALSE0",
        "push constant 2",
        "return",
        "label IF_END0"), p.vmOutput());
  }

  @Test
  void ifLabelsRestartInEachSubroutine() {
    Parser p = parser("""
        class Main {
          function void a() { if (true) { return; } return; }
          function void b() { if (false) { return; } return; }
        }
        """);
    p.parse();
    assertEquals(vm(
        "push constant 0",
        "not",
        "if-goto IF_TRUE0",
        "goto IF_FALSE0",
        "label IF_TRUE0",
        "push constant 0",
        "return",
        "label IF_FALSE0",
        "push constant 0",
        "return",
        "push constant 0",
        "if-goto IF_TRUE0",
        "goto IF_FALSE0",
        "label IF_TRUE0",
        "push constant 0",
        "return",
        "label IF_FALSE0",
        "push constant 0",
        "return"), p.vmOutput());
  }
}