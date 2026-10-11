public class Main {
  public static void main(String args[]) {
    String input = "let a = 1; // linha 1\n"
        + "/* bloco\n"
        + "de duas linhas */\n"
        + "let b = \"x\";\n";
    Scanner scan = new Scanner(input.getBytes());
    for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
      System.out.println(tk.line + ": " + tk);
    }
  }
}
