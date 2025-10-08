import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.util


class CPUTopTester extends AnyFlatSpec with ChiselScalatestTester {

  "CPUTopTester" should "pass" in {
    test(new CPUTop())
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        dut.clock.setTimeout(0)

        //Do not run the CPU
        dut.io.run.poke(false.B)

        //Load the data memory with image data
        System.out.print("\nLoading the data memory with image data... ")
        //Uncomment one of the following line depending on the image you want to load to the data memory
        //var image = Images.blackImage
        //var image = Images.whiteImage
        var image = Images.cellsImage
        //var image = Images.borderCellsImage
        for (address <- 0 to image.length - 1) {
          dut.io.testerDataMemEnable.poke(true.B)
          dut.io.testerDataMemWriteEnable.poke(true.B)
          dut.io.testerDataMemAddress.poke(address)
          dut.io.testerDataMemDataWrite.poke(image(address))
          dut.clock.step(1)
        }
        dut.io.testerDataMemEnable.poke(false.B)
        System.out.println("Done!")

        //Load the program memory with instructions
        System.out.print("\nLoading the program memory with instructions... ")
        //Uncomment one of the following line depending on the program you want to load to the program memory
        val program = Programs.program1
        //val program = Programs.program2
        for (address <- 0 to program.length - 1) {
          dut.io.testerProgMemEnable.poke(true.B)
          dut.io.testerProgMemWriteEnable.poke(true.B)
          dut.io.testerProgMemAddress.poke(address)
          dut.io.testerProgMemDataWrite.poke(program(address))
          dut.clock.step(1)
        }
        dut.io.testerProgMemEnable.poke(false.B)
        System.out.println("Done!")

        //Run the simulation of the CPU
        System.out.println("\nRun the simulation of the CPU")
        //Start the CPU
        dut.io.run.poke(true.B)
        var running = true
        var maxInstructions = 20000
        var instructionsCounter = maxInstructions
        while (running) {
          System.out.print("\rRunning cycle: " + (maxInstructions - instructionsCounter))
          dut.clock.step(1)
          instructionsCounter = instructionsCounter - 1
          running = dut.io.done.peekBoolean() == false && instructionsCounter > 0
        }
        dut.io.run.poke(false.B)
        System.out.println(" - Done!")

        //Dump the data memory content
        System.out.print("\nDump the data memory content... ")
        val inputImage = new util.ArrayList[Int]
        for (i <- 0 to 399) { //Location of the original image
          dut.io.testerDataMemEnable.poke(true.B)
          dut.io.testerDataMemWriteEnable.poke(false.B)
          dut.io.testerDataMemAddress.poke(i)
          val data = dut.io.testerDataMemDataRead.peekInt().toInt
          inputImage.add(data)
          //System.out.println("a:" + i + " d:" + data )
          dut.clock.step(1)
        }
        val outputImage = new util.ArrayList[Int]
        for (i <- 400 to 799) { //Location of the processed image
          dut.io.testerDataMemEnable.poke(true.B)
          dut.io.testerDataMemWriteEnable.poke(false.B)
          dut.io.testerDataMemAddress.poke(i)
          val data = dut.io.testerDataMemDataRead.peekInt().toInt
          outputImage.add(data)
          //System.out.println("a:" + i + " d:" + data )
          dut.clock.step(1)
        }
        dut.io.testerDataMemEnable.poke(false.B)
        System.out.println("Done!")

        System.out.print("\r\n")
        System.out.println("Input image from address 0 to 399:")
        Images.printImage(inputImage)
        System.out.println("Processed image from address 400 to 799:")
        Images.printImage(outputImage)

        System.out.println("End of simulation")

    }
  }
}
