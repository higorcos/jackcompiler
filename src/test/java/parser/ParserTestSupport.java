package parser;

import codegen.SymbolTable.Kind;
import java.nio.charset.StandardCharsets;

final class ParserTestSupport {

  private ParserTestSupport() {
  }

  static Parser parser(String input) {
    return new Parser(input.getBytes(StandardCharsets.UTF_8));
  }

  static Parser parserWithVars(String input, String... names) {
    Parser p = parser(input);
    for (String name : names) {
      p.symbolTable().define(name, "int", Kind.VAR);
    }
    return p;
  }

  static String xml(String... lines) {
    return String.join("\r\n", lines) + "\r\n";
  }

  static String vm(String... lines) {
    return String.join("\n", lines) + "\n";
  }
}