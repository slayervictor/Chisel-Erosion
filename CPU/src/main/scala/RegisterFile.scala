import chisel3._
import chisel3.util._

class RegisterFile extends Module {
  val io = IO(new Bundle {
    // 2^5 = 32 registers (RISC-V standard)
    val aSel        = Input(UInt(5.W))
    val bSel        = Input(UInt(5.W))
    val writeData   = Input(UInt(32.W))
    val writeSel    = Input(UInt(5.W))
    val writeEnable = Input(Bool())

    val a = Output(UInt(32.W))
    val b = Output(UInt(32.W))
  })

  val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  when(io.writeEnable && io.writeSel =/= 0.U) {
    regs(io.writeSel) := io.writeData
  }

  io.a := Mux(io.aSel === 0.U, 0.U, regs(io.aSel))
  io.b := Mux(io.bSel === 0.U, 0.U, regs(io.bSel))

}
