package lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScannerFilesTest {

  private static byte[] resource(String name) throws IOException {
    try (InputStream in = ScannerFilesTest.class.getResourceAsStream("/" + name)) {
      if (in == null) {
        throw new IOException("resource not found: " + name);
      }
      return in.readAllBytes();
    }
  }

  private static String normalizeXml(String content) {
    return content.lines()
        .map(String::stripLeading)
        .collect(Collectors.joining("\n"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Main", "Square", "SquareGame"})
  void matchesOfficialXml(String name) throws IOException {
    String output = TokenXml.of(resource("input/" + name + ".jack"));
    String expected = new String(resource("expected/lexer/" + name + "T.xml"), StandardCharsets.UTF_8);
    assertEquals(normalizeXml(expected), normalizeXml(output));
  }
}