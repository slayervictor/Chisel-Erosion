import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ControlUnitTester extends AnyFlatSpec with ChiselScalatestTester {
  "ControlUnit" should "set correct control signals for various instructions" in {
    test(new ControlUnit) { c =>
      // ---- R-TYPE ----
      c.io.opcode.poke("b0110011".U)
      c.io.funct3.poke("b000".U)
      c.io.funct7.poke("b0000000".U)
      c.clock.step(1)
      c.io.regWrite.expect(true.B)
      c.io.aluSrc.expect(false.B)
      c.io.aluControl.expect("b0000".U)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)

      // ---- I-TYPE ----
      c.io.opcode.poke("b0010011".U)
      c.io.funct3.poke("b010".U)
      c.clock.step(1)
      c.io.regWrite.expect(true.B)
      c.io.aluSrc.expect(true.B)
      c.io.aluControl.expect("b0010".U)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)

      // ---- LOAD ----
      c.io.opcode.poke("b0000011".U)
      c.clock.step(1)
      c.io.regWrite.expect(true.B)
      c.io.memRead.expect(true.B)
      c.io.memToReg.expect(true.B)
      c.io.aluSrc.expect(true.B)
      c.io.aluControl.expect("b0000".U)
      c.io.memWrite.expect(false.B)
      c.io.branch.expect(false.B)

      // ---- STORE ----
      c.io.opcode.poke("b0100011".U)
      c.clock.step(1)
      c.io.memWrite.expect(true.B)
      c.io.aluSrc.expect(true.B)
      c.io.aluControl.expect("b0000".U)
      c.io.regWrite.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)

      // ---- BRANCH ----
      c.io.opcode.poke("b1100011".U)
      c.clock.step(1)
      c.io.branch.expect(true.B)
      c.io.aluControl.expect("b0001".U)
      c.io.regWrite.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)

      // ---- ECALL ----
      c.io.opcode.poke("b1110011".U)
      c.io.funct3.poke("h0".U)
      c.io.funct7.poke("h0".U)
      c.clock.step(1)
      c.io.regWrite.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.branch.expect(false.B)
      c.io.aluControl.expect("b0000".U)

      // ---- JAL ----
      c.io.opcode.poke("b1101111".U)
      c.clock.step(1)
      c.io.regWrite.expect(true.B)
      c.io.aluSrc.expect(false.B)
      c.io.aluControl.expect("b0000".U)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }
}
