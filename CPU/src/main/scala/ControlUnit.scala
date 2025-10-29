import chisel3._
import chisel3.util._

class ControlUnit extends Module {
  val io = IO(new Bundle {
    //Define the module interface here (inputs/outputs)
    val instruction = Input(UInt(32.W))
    val opcode = Output(UInt(7.W))
  })

  //Implement this module here

}