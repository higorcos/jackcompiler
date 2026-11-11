import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import lexer.LexicalException;
import lexer.TokenXml;
import parser.Parser;
import parser.SemanticException;
import parser.SyntaxException;

public class Main {
  public static void main(String[] args) throws Exception {
    boolean tokenMode = args.length > 0 && args[0].equals("--tokens");
    boolean xmlMode = args.length > 0 && args[0].equals("--xml");
    boolean xmlOutput = tokenMode || xmlMode;
    int first = xmlOutput ? 1 : 0;
    int count = args.length - first;
    if (count < 1 || count > (xmlOutput ? 2 : 1)) {
      System.err.println("usage: java -cp target/classes Main [--tokens | --xml] <file.jack | directory> [output.xml]");
      System.exit(1);
    }

    Path input = Paths.get(args[first]);
    if (!Files.exists(input)) {
      System.err.println("no such file or directory: " + input);
      System.exit(1);
    }
    if (count == 2 && Files.isDirectory(input)) {
      System.err.println("an output path requires a single .jack file");
      System.exit(1);
    }
    List<Path> sources = sources(input);
    if (sources.isEmpty()) {
      System.err.println("no .jack files in " + input);
      System.exit(1);
    }

    for (Path source : sources) {
      Path target;
      if (xmlOutput) {
        target = count == 2 ? Paths.get(args[first + 1]) : defaultXmlOutput(source, tokenMode);
      } else {
        target = vmOutput(source);
      }
      String output;
      try {
        output = generate(Files.readAllBytes(source), tokenMode, xmlMode);
      } catch (LexicalException e) {
        System.err.println(source + ": lexical error at " + e.getMessage());
        System.exit(1);
        return;
      } catch (SyntaxException e) {
        System.err.println(source + ": syntax error at " + e.getMessage());
        System.exit(1);
        return;
      } catch (SemanticException e) {
        System.err.println(source + ": semantic error at " + e.getMessage());
        System.exit(1);
        return;
      }

      if (target.getParent() != null) {
        Files.createDirectories(target.getParent());
      }
      Files.writeString(target, output);
      System.out.println("wrote " + target);
    }
  }

  private static List<Path> sources(Path input) throws IOException {
    if (!Files.isDirectory(input)) {
      return List.of(input);
    }
    try (Stream<Path> files = Files.list(input)) {
      return files
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".jack"))
          .sorted()
          .toList();
    }
  }

  private static Path defaultXmlOutput(Path source, boolean tokenMode) {
    return source.resolveSibling(baseName(source) + (tokenMode ? "T.xml" : "P.xml"));
  }

  private static Path vmOutput(Path source) {
    return source.resolveSibling(baseName(source) + ".vm");
  }

  private static String baseName(Path source) {
    return source.getFileName().toString().replaceFirst("\\.jack$", "");
  }

  private static String generate(byte[] input, boolean tokenMode, boolean xmlMode) {
    if (tokenMode) {
      return TokenXml.of(input);
    }
    Parser parser = new Parser(input);
    parser.parse();
    return xmlMode ? parser.xmlOutput() : parser.vmOutput();
  }
}