import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.util

class HelloTester extends AnyFlatSpec with ChiselScalatestTester {

  "HelloTester" should "pass" in {
    test(new Hello())
      .withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

        dut.clock.setTimeout(0)
        
        //Do not run the test yet
        dut.io.run.poke(false.B)

        //Load the data memory with image data
        System.out.print("Loading the data memory with image data... ")
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

        //Run the test for 1000 cycles or till the done flag goes high
        System.out.println("Running the Hello Tester")
        dut.io.run.poke(true.B)
        var running = true
        var maxCycles = 1000
        var cyclesCounter = maxCycles
        while (running) {
          System.out.print("\rRunning cycle: " + (maxCycles - cyclesCounter))
          dut.clock.step(1)
          cyclesCounter = cyclesCounter - 1
          running = dut.io.done.peekBoolean() == false && cyclesCounter > 0
        }
        dut.io.run.poke(false.B)
        System.out.println(" - Done!")

        //Dump the data memory content
        System.out.print("Dump the data memory content... ")
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

