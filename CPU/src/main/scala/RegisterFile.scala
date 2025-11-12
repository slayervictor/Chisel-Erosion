import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    val aSel = Input(UInt(4.W))
    val bSel = Input(UInt(4.W))
    val writeData = Input(UInt(32.W))
    val writeSel = Input(UInt(4.W))
    val writeEnable = Input(Bool())

    val a = Output(UInt(32.W))
    val b = Output(UInt(32.W))
  })

  val registers = Reg(Vec(8, UInt(32.W)))

  when(io.writeEnable) {
    registers(io.writeSel) := io.writeData
  }

  io.a := registers(io.aSel)
  io.b := registers(io.bSel)

}
