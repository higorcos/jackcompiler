package codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import codegen.VMWriter.Command;
import codegen.VMWriter.Segment;
import org.junit.jupiter.api.Test;

class VMWriterTest {

  @Test
  void writePushConstant() {
    VMWriter writer = new VMWriter();
    writer.writePush(Segment.CONST, 5);
    assertEquals("push constant 5\n", writer.output());
  }

  @Test
  void writePushAndPopUseVmSegmentNames() {
    VMWriter writer = new VMWriter();
    writer.writePush(Segment.ARG, 1);
    writer.writePush(Segment.LOCAL, 2);
    writer.writePush(Segment.STATIC, 3);
    writer.writePush(Segment.THIS, 4);
    writer.writePop(Segment.THAT, 0);
    writer.writePop(Segment.POINTER, 1);
    writer.writePop(Segment.TEMP, 0);
    assertEquals("""
        push argument 1
        push local 2
        push static 3
        push this 4
        pop that 0
        pop pointer 1
        pop temp 0
        """, writer.output());
  }

  @Test
  void writeArithmeticCommands() {
    VMWriter writer = new VMWriter();
    for (Command command : Command.values()) {
      writer.writeArithmetic(command);
    }
    assertEquals("""
        add
        sub
        neg
        eq
        gt
        lt
        and
        or
        not
        """, writer.output());
  }

  @Test
  void writeFlowCommands() {
    VMWriter writer = new VMWriter();
    writer.writeLabel("WHILE_EXP0");
    writer.writeIf("WHILE_END0");
    writer.writeGoto("WHILE_EXP0");
    assertEquals("""
        label WHILE_EXP0
        if-goto WHILE_END0
        goto WHILE_EXP0
        """, writer.output());
  }

  @Test
  void writeCall() {
    VMWriter writer = new VMWriter();
    writer.writeCall("Math.abs", 1);
    assertEquals("call Math.abs 1\n", writer.output());
  }

  @Test
  void writeFunctionAndReturn() {
    VMWriter writer = new VMWriter();
    writer.writeFunction("Main.main", 2);
    writer.writeReturn();
    assertEquals("""
        function Main.main 2
        return
        """, writer.output());
  }
}