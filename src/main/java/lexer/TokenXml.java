package lexer;

public final class TokenXml {

  private TokenXml() {
  }

  public static String of(byte[] input) {
    Scanner scan = new Scanner(input);
    StringBuilder xml = new StringBuilder("<tokens>\r\n");
    for (Token tk = scan.nextToken(); tk.type != TokenType.EOF; tk = scan.nextToken()) {
      xml.append(tk).append("\r\n");
    }
    return xml.append("</tokens>\r\n").toString();
  }
}