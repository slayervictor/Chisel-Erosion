import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ALUTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ALU"

  it should "perform ADD" in {
    test(new ALU) { dut =>
      // ADD (funct7=0, funct3=0)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(5.U)
      dut.io.funct3.poke("h00".U)
      dut.io.funct7.poke("h00".U)
      dut.clock.step()
      dut.io.result.expect(15.U)
    }
  }

  it should "perform SUB" in {
    test(new ALU) { dut =>
      // SUB (funct7=other, funct3=0)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(5.U)
      dut.io.funct3.poke("h00".U)
      dut.io.funct7.poke("h20".U)
      dut.clock.step()
      dut.io.result.expect(5.U)
    }
  }

  it should "perform MUL" in {
    test(new ALU) { dut =>
      // MUL (funct7=1, funct3=0)
      dut.io.operandA.poke(10.U)
      dut.io.operandB.poke(5.U)
      dut.io.funct3.poke("h00".U)
      dut.io.funct7.poke("h01".U)
      dut.clock.step()
      dut.io.result.expect(50.U)
    }

  }

  it should "perform XOR" in {
    test(new ALU) { dut =>
      dut.io.operandA.poke("hF0F0F0F0".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.funct3.poke("h04".U)
      dut.io.funct7.poke("h00".U)
      dut.clock.step()
      dut.io.result.expect("hFFFFFFFF".U)
    }
  }

  it should "perform OR" in {
    test(new ALU) { dut =>
      dut.io.operandA.poke("hF0F0F0F0".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.funct3.poke("h06".U)
      dut.io.funct7.poke("h00".U)
      dut.clock.step()
      dut.io.result.expect("hFFFFFFFF".U)
    }
  }

  it should "perform AND" in {
    test(new ALU) { dut =>
      dut.io.operandA.poke("hF0F0F0F0".U)
      dut.io.operandB.poke("h0F0F0F0F".U)
      dut.io.funct3.poke("h07".U)
      dut.io.funct7.poke("h00".U)
      dut.clock.step()
      dut.io.result.expect(0.U)
    }
  }

  it should "perform SLL" in {
    test(new ALU) { dut =>
      dut.io.operandA.poke(0x00000001.U)
      dut.io.operandB.poke(4.U)
      dut.io.funct3.poke("h01".U)
      dut.io.funct7.poke("h00".U)
      dut.clock.step()
      dut.io.result.expect(16.U)
    }
  }

  it should "perform SRL" in {
    test(new ALU) { dut =>
      dut.io.operandA.poke(0x00000010.U)
      dut.io.operandB.poke(4.U)
      dut.io.funct3.poke("h05".U)
      dut.io.funct7.poke("h00".U)
      dut.clock.step()
      dut.io.result.expect(1.U)
    }
  }

  it should "perform SRA" in {
    test(new ALU) { dut =>
      dut.io.operandA.poke("hF0000000".U)
      dut.io.operandB.poke(4.U)
      dut.io.funct3.poke("h05".U)
      dut.io.funct7.poke("h20".U)
      dut.clock.step()
      dut.io.result.expect("hFF000000".U)
    }
  }
}
