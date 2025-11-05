import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val opcode   = Input(UInt(7.W))
    val funct3   = Input(UInt(3.W))
    val funct7   = Input(UInt(7.W))
    val aluSrc   = Output(Bool())
    val regWrite = Output(Bool())
    val memRead  = Output(Bool())
    val memWrite = Output(Bool())
    val memToReg = Output(Bool())
    val branch   = Output(Bool())
  })

  // default outputs
  io.aluSrc   := false.B
  io.regWrite := false.B
  io.memRead  := false.B
  io.memWrite := false.B
  io.memToReg := false.B
  io.branch   := false.B

  // local aliases (read-only)
  val opcode = io.opcode
  val funct3 = io.funct3
  val funct7 = io.funct7

  // opcodes
  val R_TYPE = "b0110011".U
  val I_TYPE = "b0010011".U
  val LOAD   = "b0000011".U
  val STORE  = "b0100011".U
  val BRANCH = "b1100011".U
  val JAL    = "b1101111".U
  val ECALL  = "b1110011".U

  switch(opcode) {
    is(R_TYPE) {
      io.regWrite := true.B
      io.aluSrc   := false.B
      // funct3/funct7 are available via io.funct3 / io.funct7 for downstream units
    }
    is(I_TYPE) {
      io.regWrite := true.B
      io.aluSrc   := true.B
    }
    is(LOAD) {
      io.regWrite := true.B
      io.aluSrc   := true.B
      io.memRead  := true.B
      io.memToReg := true.B
    }
    is(STORE) {
      io.aluSrc   := true.B
      io.memWrite := true.B
    }
    is(BRANCH) {
      io.branch := true.B
    }
    is(JAL) {
      io.regWrite := true.B // write return address to rd
      io.aluSrc   := false.B
    }
    is(ECALL) {
      // leave defaults (all false). If you need a special signal for ecall, add it in IO.
    }
  }
}
