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
    val li = Output(Bool())
    val lb = Output(Bool())
    val sb = Output(Bool())
    val branch = Output(Bool())
    val add = Output(Bool())
    val addi = Output(Bool())
    val j = Output(Bool())
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
  io.add := false.B
  io.addi := false.B
  io.li := false.B
  io.lb := false.B
  io.sb := false.B
  io.branch := false.B
  io.j := false.B
  io.exit := false.B

  switch(opcode) {
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
    // Add
    is("b0001".U) {
      io.add := true.B
    }
    // Add Immediate
    is("b0010".U) {
      io.addi := true.B
    }
    // Jump
    is("b0111".U) {
      io.j := true.B
    }
    // Exit
    is("b1000".U) {
      io.exit := true.B
    }
  }
}
