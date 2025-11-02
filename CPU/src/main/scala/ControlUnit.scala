import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    val opcode     = Input(UInt(7.W))
    val funct3     = Input(UInt(3.W))
    val funct7     = Input(UInt(7.W))
    val aluControl = Output(UInt(4.W))
    val aluSrc     = Output(Bool()) 
    val regWrite   = Output(Bool())  
    val memRead    = Output(Bool())
    val memWrite   = Output(Bool())
    val memToReg   = Output(Bool())
    val branch     = Output(Bool())
  })

  // Default values
  io.aluControl := 0.U
  io.aluSrc     := false.B
  io.regWrite   := false.B
  io.memRead    := false.B
  io.memWrite   := false.B
  io.memToReg   := false.B
  io.branch     := false.B

  // Opcodes
  val R_TYPE = "b0110011".U
  val I_TYPE = "b0010011".U
  val LOAD   = "b0000011".U
  val STORE  = "b0100011".U
  val BRANCH = "b1100011".U

  switch(io.opcode) {
    is(R_TYPE) {
      io.regWrite := true.B
      io.aluSrc   := false.B
      io.aluControl := Cat(io.funct7(5), io.funct3)
    }

    is(I_TYPE) {
      io.regWrite := true.B
      io.aluSrc   := true.B
      io.aluControl := Cat(0.U(1.W), io.funct3) 
    }

    is(LOAD) {
      io.regWrite := true.B
      io.aluSrc   := true.B
      io.memRead  := true.B
      io.memToReg := true.B
      io.aluControl := "b0000".U 
    }

    is(STORE) {
      io.aluSrc   := true.B
      io.memWrite := true.B
      io.aluControl := "b0000".U 
    }

    is(BRANCH) {
      io.branch := true.B
      io.aluControl := "b0001".U
    }
  }
}