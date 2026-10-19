package parser;

public class SyntaxException extends RuntimeException {
  private final int line;

  public SyntaxException(String message, int line) {
    super("line " + line + ": " + message);
    this.line = line;
  }

  public int getLine() {
    return line;
  }
}