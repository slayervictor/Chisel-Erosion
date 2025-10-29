import chisel3._
import chisel3.util._

class ProgramCounter extends Module {
  val io = IO(new Bundle {
    val stop = Input(Bool())
    val jump = Input(Bool())
    val run = Input(Bool())
    val programCounterJump = Input(UInt(16.W))
    val programCounter = Output(UInt(16.W))
    val regFile = Mem(32, UInt(32.W))
  })

  val counterReg = RegInit(0.U(16.W))

  when(io.jump) {
    counterReg := io.programCounterJump
  }.elsewhen(io.run && !io.stop) {
    counterReg := counterReg + 1.U
  }

  io.programCounter := counterReg

}

