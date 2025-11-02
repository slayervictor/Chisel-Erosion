import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val aluControl = Input(UInt(4.W)) // { funct7[5], funct3[2:0] }
    val result = Output(UInt(32.W))
  })


  // Extract control signals for readability
  val funct3 = io.aluControl(2, 0)
  val funct7 = io.aluControl(3)

  // Default value
  io.result := 0.U

  switch(funct3) {
    is("h0".U) {
      when(funct7 === "h0".U) {
        io.result := io.operandA + io.operandB // ADD
      }.otherwise {
        io.result := io.operandA - io.operandB // SUB
      }
    }

    is("h4".U) { // XOR
      io.result := io.operandA ^ io.operandB
    }

    is("h6".U) { // OR
      io.result := io.operandA | io.operandB
    }

    is("h1".U) { // SLL (Shift Left Logical)
      io.result := io.operandA << io.operandB(4, 0) // Only lower 5 bits
    }

    is("h2".U) { // SLT (Set Less Than Signed)
      io.result := Mux(io.operandA.asSInt < io.operandB.asSInt, 1.U, 0.U)
    }

    is("h3".U) { // SLTU (Set Less Than Unsigned)
      io.result := Mux(io.operandA < io.operandB, 1.U, 0.U)
    }

    is("h5".U) { // SRL or SRA (Shift Right)
      when(funct7 === "h0".U) {
        io.result := io.operandA >> io.operandB(4, 0) // SRL (Logical)
      }.otherwise {
        io.result := (io.operandA.asSInt >> io.operandB(
          4,
          0
        )).asUInt // SRA (Arithmetic)
      }
    }
    is("h7".U) { // AND
      io.result := io.operandA & io.operandB
    }
  }

  is("h0".U) {
    when(funct7 === "h1".U) {
      io.result := io.operandA * io.operandB
    }
  }

}
