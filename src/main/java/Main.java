import java.nio.file.Files;
import java.nio.file.Paths;
import lexer.LexicalException;
import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
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

    byte[] input = Files.readAllBytes(Paths.get(args[first]));
    String output;
    try {
      output = tokenMode ? tokens(input) : tree(input);
    } catch (LexicalException e) {
      System.err.println("lexical error at " + e.getMessage());
      System.exit(1);
      return;
    } catch (SyntaxException e) {
      System.err.println("syntax error at " + e.getMessage());
      System.exit(1);
      return;
    }

    if (count == 2) {
      Files.writeString(Paths.get(args[first + 1]), output);
    } else {
      System.out.print(output);
    }
  }

  private static String tokens(byte[] input) {
    Scanner scan = new Scanner(input);
    StringBuilder out = new StringBuilder("<tokens>\n");
    for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
      out.append(tk).append("\n");
    }
    return out.append("</tokens>\n").toString();
  }

  private static String tree(byte[] input) {
    Parser parser = new Parser(input);
    parser.parse();
    return parser.xmlOutput();
  }
}