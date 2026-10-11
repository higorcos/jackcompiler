public class Main {
  public static void main(String args[]) {
    String input = "// comentario de linha\n"
        + "let a = 4 / 2; /* bloco */ let b = a; /** doc\n"
        + "multilinha */ let c = b;";
    Scanner scan = new Scanner(input.getBytes());
    for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
      System.out.println(tk);
    }
  }
}
