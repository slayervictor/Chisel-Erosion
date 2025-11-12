import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val aSel = Input(Uint(16.W))
    val bSel = Input(Uint(16.W))
    val func = Input(Bool())
    val result = Output(Uint(16.W))
  })

  val add = io.aSel.asSInt() + io.bSel.asSInt()
  val isZero = (io.aSel === 0.U)

  io.result := Mux(io.func, isZero.asUInt(), add.asUInt())
}
