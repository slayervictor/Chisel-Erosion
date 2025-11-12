import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))

    val opcode = Output(UInt(4.W))
    val reg1 = Output(UInt(4.W))
    val reg2 = Output(UInt(4.W))
    val reg3 = Output(UInt(4.W))
    val imm = Output(UInt(16.W))
    val nop = Output(Bool())
    val li = Output(Bool())
    val lb = Output(Bool())
    val sb = Output(Bool())
    val branch = Output(Bool())
    val add = Output(Bool())
    val addi = Output(Bool())
    val mul = Output(Bool())
    val or = Output(Bool())
    val and = Output(Bool())
    val not = Output(Bool())
    val j = Output(Bool())
    val jeq = Output(Bool())
    val jlt = Output(Bool())
    val jgt = Output(Bool())
    val exit = Output(Bool())
  })

  val opcode = io.instruction(3, 0)
  val regA = io.instruction(7, 4)
  val regB = io.instruction(11, 8)
  val regC = io.instruction(15, 12)
  val imm = io.instruction(31, 16)

  // Setting the values
  io.opcode := opcode
  io.reg1 := regA
  io.reg2 := regB
  io.reg3 := regC
  io.imm := imm
  io.nop := false.B
  io.add := false.B
  io.addi := false.B
  io.mul := false.B
  io.or := false.B
  io.and := false.B
  io.not := false.B
  io.li := false.B
  io.lb := false.B
  io.sb := false.B
  io.branch := false.B
  io.j := false.B
  io.jeq := false.B
  io.jlt := false.B
  io.jgt := false.B
  io.exit := false.B

  switch(opcode) {
    // NOP
    is("b0000".U) {
      io.nop := true.B
    }
    // Add
    is("b0001".U) {
      io.add := true.B
    }
    // Add Immediate
    is("b0010".U) {
      io.addi := true.B
    }
    // Load immediate
    is("b0011".U) {
      io.li := true.B
    }
    // Load byte
    is("b0100".U) {
      io.lb := true.B
    }
    // Save byte
    is("b0101".U) {
      io.sb := true.B
    }
    // If equals zero, branch
    is("b0110".U) {
      io.branch := true.B
    }
    // Jump
    is("b0111".U) {
      io.j := true.B
    }
    // Exit
    is("b1000".U) {
      io.exit := true.B
    }
    // Multiply
    is("b1001".U) {
      io.mul := true.B
    }
    // OR
    is("b1010".U) {
      io.or := true.B
    }
    // AND
    is("b1011".U) {
      io.and := true.B
    }
    // NOT
    is("b1100".U) {
      io.not := true.B
    }
    // Jump if equal
    is("b1101".U) {
      io.jeq := true.B
    }
    // Jump if less than
    is("b1110".U) {
      io.jlt := true.B
    }
    // Jump if greater than
    is("b1111".U) {
      io.jgt := true.B
    }
  }
}
