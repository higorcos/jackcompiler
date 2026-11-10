package codegen;

public class VMWriter {

  public enum Segment {
    CONST("constant"),
    ARG("argument"),
    LOCAL("local"),
    STATIC("static"),
    THIS("this"),
    THAT("that"),
    POINTER("pointer"),
    TEMP("temp");

    private final String vmName;

    Segment(String vmName) {
      this.vmName = vmName;
    }
  }

  public enum Command {
    ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
  }

  private final StringBuilder output = new StringBuilder();

  public void writePush(Segment segment, int index) {
    output.append("push %s %d\n".formatted(segment.vmName, index));
  }

  public void writePop(Segment segment, int index) {
    output.append("pop %s %d\n".formatted(segment.vmName, index));
  }

  public void writeArithmetic(Command command) {
    output.append(command.name().toLowerCase()).append("\n");
  }

  public void writeLabel(String label) {
    output.append("label ").append(label).append("\n");
  }

  public void writeGoto(String label) {
    output.append("goto ").append(label).append("\n");
  }

  public void writeIf(String label) {
    output.append("if-goto ").append(label).append("\n");
  }

  public void writeCall(String name, int nArgs) {
    output.append("call %s %d\n".formatted(name, nArgs));
  }

  public void writeFunction(String name, int nLocals) {
    output.append("function %s %d\n".formatted(name, nLocals));
  }

  public void writeReturn() {
    output.append("return\n");
  }

  public String output() {
    return output.toString();
  }
}