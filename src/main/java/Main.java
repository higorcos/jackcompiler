import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
      System.err.println("usage: java -cp target/classes Main [--tokens | --xml] <file.jack> [output.xml]");
      System.exit(1);
    }

    Path source = Paths.get(args[first]);
    Path target;
    if (xmlOutput) {
      target = count == 2 ? Paths.get(args[first + 1]) : defaultXmlOutput(source, tokenMode);
    } else {
      target = vmOutput(source);
    }
    byte[] input = Files.readAllBytes(source);
    String output;
    try {
      if (tokenMode) {
        output = TokenXml.of(input);
      } else if (xmlMode) {
        output = tree(input);
      } else {
        output = vm(input);
      }
    } catch (LexicalException e) {
      System.err.println("lexical error at " + e.getMessage());
      System.exit(1);
      return;
    } catch (SyntaxException e) {
      System.err.println("syntax error at " + e.getMessage());
      System.exit(1);
      return;
    } catch (SemanticException e) {
      System.err.println("semantic error at " + e.getMessage());
      System.exit(1);
      return;
    }

    if (target.getParent() != null) {
      Files.createDirectories(target.getParent());
    }
    Files.writeString(target, output);
    System.out.println("wrote " + target);
  }

  private static Path defaultXmlOutput(Path source, boolean tokenMode) {
    String name = baseName(source);
    return tokenMode
        ? Paths.get("output", "lexer", name + "T.xml")
        : Paths.get("output", "parser", name + ".xml");
  }

  private static Path vmOutput(Path source) {
    return source.resolveSibling(baseName(source) + ".vm");
  }

  private static String baseName(Path source) {
    return source.getFileName().toString().replaceFirst("\\.jack$", "");
  }

  private static String tree(byte[] input) {
    Parser parser = new Parser(input);
    parser.parse();
    return parser.xmlOutput();
  }

  private static String vm(byte[] input) {
    Parser parser = new Parser(input);
    parser.parse();
    return parser.vmOutput();
  }
}