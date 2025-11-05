import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ControlUnitTester extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "ControlUnit"

  it should "set correct control signals for R-TYPE instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b0110011".U)
      c.io.funct3.poke("b000".U)
      c.io.funct7.poke("b0000000".U)
      c.clock.step(1)

      c.io.regWrite.expect(true.B)
      c.io.aluSrc.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }

  it should "set correct control signals for I-TYPE instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b0010011".U)
      c.io.funct3.poke("b010".U)
      c.clock.step(1)

      c.io.regWrite.expect(true.B)
      c.io.aluSrc.expect(true.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }

  it should "set correct control signals for LOAD instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b0000011".U)
      c.clock.step(1)

      c.io.regWrite.expect(true.B)
      c.io.memRead.expect(true.B)
      c.io.memToReg.expect(true.B)
      c.io.aluSrc.expect(true.B)
      c.io.memWrite.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }

  it should "set correct control signals for STORE instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b0100011".U)
      c.clock.step(1)

      c.io.memWrite.expect(true.B)
      c.io.aluSrc.expect(true.B)
      c.io.regWrite.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }

  it should "set correct control signals for BRANCH instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b1100011".U)
      c.clock.step(1)

      c.io.branch.expect(true.B)
      c.io.regWrite.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
    }
  }

  it should "set correct control signals for ECALL instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b1110011".U)
      c.io.funct3.poke("h0".U)
      c.io.funct7.poke("h0".U)
      c.clock.step(1)

      c.io.regWrite.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }

  it should "set correct control signals for JAL instructions" in {
    test(new ControlUnit) { c =>
      c.io.opcode.poke("b1101111".U)
      c.clock.step(1)

      c.io.regWrite.expect(true.B)
      c.io.aluSrc.expect(false.B)
      c.io.memRead.expect(false.B)
      c.io.memWrite.expect(false.B)
      c.io.memToReg.expect(false.B)
      c.io.branch.expect(false.B)
    }
  }
}
