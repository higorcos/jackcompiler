import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("usage: java -cp bin Main <file.jack>");
      System.exit(1);
    }
    byte[] input = Files.readAllBytes(Paths.get(args[0]));
    Scanner scan = new Scanner(input);
    for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
      System.out.println(tk.line + ": " + tk);
    }
  }
}
