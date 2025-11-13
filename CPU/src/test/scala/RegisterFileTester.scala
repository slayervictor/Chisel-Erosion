import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class RegisterFileTester extends AnyFlatSpec with ChiselScalatestTester {
  "RegisterFile" should "correctly write and read registers" in {
    test(new RegisterFile) { c =>
      // Write 1234 to register 5
      c.io.writeEnable.poke(true.B)
      c.io.writeSel.poke(5.U)
      c.io.writeData.poke(1234.U)
      c.clock.step(1)

      // Read from reg 5 on both ports
      c.io.writeEnable.poke(false.B)
      c.io.aSel.poke(5.U)
      c.io.bSel.poke(5.U)
      c.clock.step(1)
      c.io.a.expect(1234.U)
      c.io.b.expect(1234.U)

      // Write 9999 to reg 2
      c.io.writeEnable.poke(true.B)
      c.io.writeSel.poke(2.U)
      c.io.writeData.poke(9999.U)
      c.clock.step(1)

      // Read both reg 2 and reg 5 simultaneously
      c.io.writeEnable.poke(false.B)
      c.io.aSel.poke(2.U)
      c.io.bSel.poke(5.U)
      c.clock.step(1)
      c.io.a.expect(9999.U)
      c.io.b.expect(1234.U)

      // Ensure writeEnable = false prevents writes
      c.io.writeEnable.poke(false.B)
      c.io.writeSel.poke(2.U)
      c.io.writeData.poke(5555.U)
      c.clock.step(1)
      // read again to confirm value unchanged
      c.io.aSel.poke(2.U)
      c.clock.step(1)
      c.io.a.expect(9999.U)

      // Write and read back all registers (0–7)
      for (i <- 0 until 8) {
        c.io.writeEnable.poke(true.B)
        c.io.writeSel.poke(i.U)
        c.io.writeData.poke((i * 100).U)
        c.clock.step(1)
      }

      // disable write before readback
      c.io.writeEnable.poke(false.B)

      for (i <- 0 until 8) {
        c.io.aSel.poke(i.U)
        c.io.bSel.poke((7 - i).U)
        c.clock.step(1)
        c.io.a.expect((i * 100).U)
        c.io.b.expect(((7 - i) * 100).U)
      }
    }
  }
}
