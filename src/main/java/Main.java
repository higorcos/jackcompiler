import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lexer.LexicalException;
import lexer.TokenXml;
import parser.Parser;
import parser.SyntaxException;

public class Main {
  public static void main(String[] args) throws Exception {
    boolean tokenMode = args.length > 0 && args[0].equals("--tokens");
    int first = tokenMode ? 1 : 0;
    int count = args.length - first;
    if (count < 1 || count > 2) {
      System.err.println("usage: java -cp target/classes Main [--tokens] <file.jack> [output.xml]");
      System.exit(1);
    }

    Path source = Paths.get(args[first]);
    Path target = count == 2 ? Paths.get(args[first + 1]) : defaultOutput(source, tokenMode);
    byte[] input = Files.readAllBytes(source);
    String output;
    try {
      output = tokenMode ? TokenXml.of(input) : tree(input);
    } catch (LexicalException e) {
      System.err.println("lexical error at " + e.getMessage());
      System.exit(1);
      return;
    } catch (SyntaxException e) {
      System.err.println("syntax error at " + e.getMessage());
      System.exit(1);
      return;
    }

    if (target.getParent() != null) {
      Files.createDirectories(target.getParent());
    }
    Files.writeString(target, output);
    System.out.println("wrote " + target);
  }

  private static Path defaultOutput(Path source, boolean tokenMode) {
    String name = source.getFileName().toString().replaceFirst("\\.jack$", "");
    return tokenMode
        ? Paths.get("output", "lexer", name + "T.xml")
        : Paths.get("output", "parser", name + ".xml");
  }

  private static String tree(byte[] input) {
    Parser parser = new Parser(input);
    parser.parse();
    return parser.xmlOutput();
  }
}