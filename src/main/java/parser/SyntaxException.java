package parser;

public class SyntaxException extends RuntimeException {
  private final int line;
  private final int column;

  public SyntaxException(String message, int line, int column) {
    super("line " + line + ", column " + column + ": " + message);
    this.line = line;
    this.column = column;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }
}