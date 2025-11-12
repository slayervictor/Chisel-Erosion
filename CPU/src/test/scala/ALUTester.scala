import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ALUTester extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "ALU"

    it should "perform basic addition with positive numbers" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(10.U)
            dut.io.bSel.poke(5.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(15.U)
        }
    }

    it should "add zero to a positive number" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(42.U)
            dut.io.bSel.poke(0.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(42.U)
        }
    }

    it should "add two zeros" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0.U)
            dut.io.bSel.poke(0.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(0.U)
        }
    }

    it should "perform addition with maximum 16-bit values" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(100.U)
            dut.io.bSel.poke(200.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(300.U)
        }
    }

    it should "handle signed addition with negative numbers (two's complement)" in {
        test(new ALU) { dut =>
            // -1 in 16-bit two's complement = 0xFFFF
            dut.io.aSel.poke(0xffff.U)
            dut.io.bSel.poke(0xffff.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(0xfffe.U)
        }
    }

    it should "add positive and negative numbers" in {
        test(new ALU) { dut =>
            // 10 + (-5) = 5
            dut.io.aSel.poke(10.U)
            dut.io.bSel.poke(0xfffb.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(5.U)
        }
    }

    it should "handle addition overflow" in {
        test(new ALU) { dut =>
            // Max positive 16-bit signed: 32767 (0x7FFF)
            dut.io.aSel.poke(0x7fff.U)
            dut.io.bSel.poke(1.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(0x8000.U)
        }
    }

    it should "handle addition underflow" in {
        test(new ALU) { dut =>
            // Min negative 16-bit signed: -32768 (0x8000)
            dut.io.aSel.poke(0x8000.U)
            dut.io.bSel.poke(0xffff.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(0x7fff.U)
        }
    }

    it should "add large positive numbers" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(1000.U)
            dut.io.bSel.poke(2000.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(3000.U)
        }
    }

    it should "detect zero correctly when aSel is zero" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0.U)
            dut.io.bSel.poke(0.U)
            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(1.U) // isZero = true
        }
    }

    it should "detect non-zero correctly when aSel is positive" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(42.U)
            dut.io.bSel.poke(0.U)
            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(0.U) // isZero = false
        }
    }

    it should "detect non-zero correctly when aSel is 1" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(1.U)
            dut.io.bSel.poke(100.U)
            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(0.U) // isZero = false
        }
    }

    it should "detect non-zero correctly when aSel is maximum value" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0xffff.U)
            dut.io.bSel.poke(0.U)
            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(0.U) // isZero = false
        }
    }

    it should "ignore bSel when checking for zero" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0.U)
            dut.io.bSel.poke(9999.U) // bSel should be ignored
            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(1.U) // isZero = true
        }
    }

    it should "detect non-zero for various non-zero values" in {
        test(new ALU) { dut =>
            val testValues = Seq(1, 2, 10, 100, 1000, 0x7fff, 0x8000, 0xffff)

            testValues.foreach { value =>
                dut.io.aSel.poke(value.U)
                dut.io.bSel.poke(0.U)
                dut.io.func.poke(5.U) // isZero
                dut.clock.step()
                dut.io.result.expect(0.U) // All should be non-zero
            }
        }
    }

    it should "switch between addition and zero check" in {
        test(new ALU) { dut =>
            // Test addition
            dut.io.aSel.poke(5.U)
            dut.io.bSel.poke(3.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(8.U)

            // Switch to zero check with non-zero value
            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(0.U)

            // Zero check with zero value
            dut.io.aSel.poke(0.U)
            dut.clock.step()
            dut.io.result.expect(1.U)

            // Back to addition
            dut.io.aSel.poke(10.U)
            dut.io.bSel.poke(20.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(30.U)
        }
    }

    it should "handle rapid function switching" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0.U)
            dut.io.bSel.poke(5.U)

            // Alternate between functions
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(5.U) // 0 + 5 = 5

            dut.io.func.poke(5.U) // isZero
            dut.clock.step()
            dut.io.result.expect(1.U) // 0 is zero = true

            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(5.U) // 0 + 5 = 5
        }
    }

    it should "handle all bits set" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0xffff.U)
            dut.io.bSel.poke(0xffff.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            // -1 + -1 = -2 = 0xFFFE
            dut.io.result.expect(0xfffe.U)
        }
    }

    it should "handle alternating bit patterns" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0xaaaa.U)
            dut.io.bSel.poke(0x5555.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(0xffff.U)
        }
    }

    it should "handle single bit patterns" in {
        test(new ALU) { dut =>
            // Test with single bit set
            dut.io.aSel.poke(0x0001.U)
            dut.io.bSel.poke(0x0001.U)
            dut.io.func.poke(0.U) // add
            dut.clock.step()
            dut.io.result.expect(0x0002.U)
        }
    }

    it should "perform multiplication" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(5.U)
            dut.io.bSel.poke(3.U)
            dut.io.func.poke(1.U) // mult
            dut.clock.step()
            dut.io.result.expect(15.U)
        }
    }

    it should "perform OR operation" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0xaaaa.U)
            dut.io.bSel.poke(0x5555.U)
            dut.io.func.poke(2.U) // or
            dut.clock.step()
            dut.io.result.expect(0xffff.U)
        }
    }

    it should "perform AND operation" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0xffff.U)
            dut.io.bSel.poke(0x5555.U)
            dut.io.func.poke(3.U) // and
            dut.clock.step()
            dut.io.result.expect(0x5555.U)
        }
    }

    it should "perform NOT operation" in {
        test(new ALU) { dut =>
            dut.io.aSel.poke(0.U) // aSel ignored for NOT
            dut.io.bSel.poke(0xaaaa.U)
            dut.io.func.poke(4.U) // not
            dut.clock.step()
            dut.io.result.expect(0x5555.U)
        }
    }
}
