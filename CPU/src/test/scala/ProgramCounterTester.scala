import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.util

class ProgramCounterTester extends AnyFlatSpec with ChiselScalatestTester {

  "ProgramCounterTester" should "pass" in {
    test(new ProgramCounter())
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        dut.clock.setTimeout(0)

        //Program Counter running for 5 clock cycles
        dut.io.jump.poke(false.B)
        dut.io.run.poke(true.B)
        dut.io.stop.poke(false.B)
        dut.io.programCounterJump.poke(0)
        dut.clock.step(5)

        //Hold for 5 clock cycles
        dut.io.jump.poke(false.B)
        dut.io.run.poke(true.B)
        dut.io.stop.poke(true.B)
        dut.io.programCounterJump.poke(0)
        dut.clock.step(5)

        //Hold for 5 clock cycles
        dut.io.jump.poke(false.B)
        dut.io.run.poke(false.B)
        dut.io.stop.poke(false.B)
        dut.io.programCounterJump.poke(0)
        dut.clock.step(5)

        //Load the value 30
        dut.io.jump.poke(true.B)
        dut.io.run.poke(true.B)
        dut.io.stop.poke(false.B)
        dut.io.programCounterJump.poke(30)
        dut.clock.step(1)

        //Program Counter running for another 5 clock cycles
        dut.io.jump.poke(false.B)
        dut.io.run.poke(true.B)
        dut.io.stop.poke(false.B)
        dut.io.programCounterJump.poke(0)
        dut.clock.step(5)

    }
  }
}

