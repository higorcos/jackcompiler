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

  @Test
  void functionCallWithoutArguments() {
    Parser p = parser("Keyboard.keyPressed();");
    p.parseExpression();
    assertEquals(vm("call Keyboard.keyPressed 0"), p.vmOutput());
  }

  @Test
  void functionCallWithArguments() {
    Parser p = parser("Math.max(1, 2);");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "call Math.max 2"), p.vmOutput());
  }

  @Test
  void functionCallInsideExpression() {
    Parser p = parser("1 + Math.abs(-2);");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "neg",
        "call Math.abs 1",
        "add"), p.vmOutput());
  }

  @Test
  void nestedFunctionCalls() {
    Parser p = parser("Math.max(Math.min(1, 2), 3);");
    p.parseExpression();
    assertEquals(vm(
        "push constant 1",
        "push constant 2",
        "call Math.min 2",
        "push constant 3",
        "call Math.max 2"), p.vmOutput());
  }

  @Test
  void constructorAllocatesFieldsAndAnchorsThis() {
    Parser p = parser("""
        class Point {
          field int x, y;
          static int count;
          constructor Point new(int ax, int ay) {
            let x = ax;
            let y = ay;
            return this;
          }
          function int zero() { return 0; }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Point.new 0",
        "push constant 2",
        "call Memory.alloc 1",
        "pop pointer 0",
        "push argument 0",
        "pop this 0",
        "push argument 1",
        "pop this 1",
        "push pointer 0",
        "return",
        "function Point.zero 0",
        "push constant 0",
        "return"), p.vmOutput());
  }

  @Test
  void methodAnchorsThisInFirstArgument() {
    Parser p = parser("""
        class Point {
          field int x;
          method int getx() { return x; }
          method void setx(int v) { let x = v; return; }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Point.getx 0",
        "push argument 0",
        "pop pointer 0",
        "push this 0",
        "return",
        "function Point.setx 0",
        "push argument 0",
        "pop pointer 0",
        "push argument 1",
        "pop this 0",
        "push constant 0",
        "return"), p.vmOutput());
  }

  @Test
  void methodCallOnField() {
    Parser p = parser("""
        class Game {
          field Square square;
          method void run() { do square.moveUp(); return; }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Game.run 0",
        "push argument 0",
        "pop pointer 0",
        "push this 0",
        "call Square.moveUp 1",
        "pop temp 0",
        "push constant 0",
        "return"), p.vmOutput());
  }

  @Test
  void methodCallOnLocalWithArguments() {
    Parser p = parser("""
        class Main {
          function int f() {
            var Point p;
            let p = Point.new(1, 2);
            return p.plus(3, 4);
          }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Main.f 1",
        "push constant 1",
        "push constant 2",
        "call Point.new 2",
        "pop local 0",
        "push local 0",
        "push constant 3",
        "push constant 4",
        "call Point.plus 3",
        "return"), p.vmOutput());
  }

  @Test
  void methodCallOnCurrentObject() {
    Parser p = parser("""
        class Game {
          method void run() { do draw(1); return; }
          method void draw(int n) { return; }
        }
        """);
    p.parse();
    assertEquals(vm(
        "function Game.run 0",
        "push argument 0",
        "pop pointer 0",
        "push pointer 0",
        "push constant 1",
        "call Game.draw 2",
        "pop temp 0",
        "push constant 0",
        "return",
        "function Game.draw 0",
        "push argument 0",
        "pop pointer 0",
        "push constant 0",
        "return"), p.vmOutput());
  }
}