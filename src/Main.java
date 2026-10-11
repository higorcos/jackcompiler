public class Main {
  public static void main(String args[]) {
    String input = "{ } ( ) [ ] . , ; + - * / & | < > = ~";
    Scanner scan = new Scanner(input.getBytes());
    for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
      System.out.println(tk);
    }
  }
}
