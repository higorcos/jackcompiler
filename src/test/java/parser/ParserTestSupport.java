package parser;

import java.nio.charset.StandardCharsets;

final class ParserTestSupport {

  private ParserTestSupport() {
  }

  static Parser parser(String input) {
    return new Parser(input.getBytes(StandardCharsets.UTF_8));
  }

  static String xml(String... lines) {
    return String.join("\r\n", lines) + "\r\n";
  }
}