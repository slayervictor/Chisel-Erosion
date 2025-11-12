import chisel3._
import chisel3.util._

class ALU extends Module {
    val io = IO(new Bundle {
        val aSel   = Input(UInt(16.W))
        val bSel   = Input(UInt(16.W))
        val func   = Input(UInt(3.W))
        val result = Output(UInt(16.W))
    })

    val add    = io.aSel.asSInt + io.bSel.asSInt
    val mult   = io.aSel.asSInt * io.bSel.asSInt
    val or     = io.aSel.asSInt | io.bSel.asSInt
    val and    = io.aSel.asSInt & io.bSel.asSInt
    val not    = ~io.bSel.asSInt
    val isZero = (io.aSel === 0.U)

    // MuxLookup or switch to select operation
    io.result := MuxLookup(io.func, 0.U)(
      Seq(
        0.U -> add.asUInt,
        1.U -> mult.asUInt,
        2.U -> or.asUInt,
        3.U -> and.asUInt,
        4.U -> not.asUInt,
        5.U -> isZero.asUInt
      )
    )
}
