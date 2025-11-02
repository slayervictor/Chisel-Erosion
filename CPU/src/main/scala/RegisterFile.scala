import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    // 2^4 = 16
    val aSel = Input(UInt(4.W))
    val bSel = Input(UInt(4.W))
    val writeData = Input(UInt(32.W))
    val writeSel = Input(UInt(4.W))
    val writeEnable = Input(Bool())

    val a = Output(UInt(32.W))
    val b = Output(UInt(32.W))
  })

  val regs = Reg(Vec(16, UInt(32.W)))

  when(io.writeEnable) {
    regs(io.writeSel) := io.writeData
  }

  io.a := regs(io.aSel)
  io.b := regs(io.bSel)

}
