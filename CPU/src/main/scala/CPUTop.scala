import chisel3._
import chisel3.util._

class CPUTop extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool())
    val run = Input(Bool())
    // This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerDataMemEnable = Input(Bool())
    val testerDataMemAddress = Input(UInt(16.W))
    val testerDataMemDataRead = Output(UInt(32.W))
    val testerDataMemWriteEnable = Input(Bool())
    val testerDataMemDataWrite = Input(UInt(32.W))
    // This signals are used by the tester for loading and dumping the memory content, do not touch
    val testerProgMemEnable = Input(Bool())
    val testerProgMemAddress = Input(UInt(16.W))
    val testerProgMemDataRead = Output(UInt(32.W))
    val testerProgMemWriteEnable = Input(Bool())
    val testerProgMemDataWrite = Input(UInt(32.W))
  })

  // Creating components
  val programCounter = Module(new ProgramCounter())
  val dataMemory = Module(new DataMemory())
  val programMemory = Module(new ProgramMemory())
  val registerFile = Module(new RegisterFile())
  val controlUnit = Module(new ControlUnit())
  val alu = Module(new ALU())

  // Connecting the modules
  io.run := false.B
  programCounter.io.run := io.run
  programMemory.io.address := programCounter.io.programCounter
  programCounter.io.run := io.run
  programCounter.io.jump := false.B
  programCounter.io.stop := false.B
  programCounter.io.programCounterJump := 0.U

  // CONTROL UNIT
  controlUnit.io.instruction := programMemory.io.instructionRead

  // ALU
  alu.io.aSel := 0.U
  alu.io.bSel := 0.U
  alu.io.func := false.B

  // MEMORY
  dataMemory.io.address := 0.U
  dataMemory.io.writeEnable := false.B
  dataMemory.io.dataWrite := 0.U
  programMemory.io.address := programCounter.io.programCounter

  // REGISTERFILE
  registerFile.io.aSel := 0.U
  registerFile.io.bSel := 0.U
  registerFile.io.writeData := 0.U
  registerFile.io.writeSel := 0.U
  registerFile.io.writeEnable := false.B

  // INSTRUCTIONS
  when(controlUnit.io.add) {
    alu.io.func := false.B
    registerFile.io.aSel := controlUnit.io.reg2
    registerFile.io.bSel := controlUnit.io.reg3
    alu.io.aSel := registerFile.io.aSel
    alu.io.bSel := registerFile.io.bSel

    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result
  }
    .elsewhen(controlUnit.io.li) {
      registerFile.io.writeEnable := true.B
      registerFile.io.writeSel := controlUnit.io.reg1
      registerFile.io.writeData := controlUnit.io.imm
    }
    .elsewhen(controlUit.io.branch) {
      alu.io.func := true.B
      registerFile.io.aSel := controlUnit.io.reg1
      alu.io.aSel := registerFile.io.aSel
    }

  // This signals are used by the tester for loading the program to the program memory, do not touch
  programMemory.io.testerAddress := io.testerProgMemAddress
  io.testerProgMemDataRead := programMemory.io.testerDataRead
  programMemory.io.testerDataWrite := io.testerProgMemDataWrite
  programMemory.io.testerEnable := io.testerProgMemEnable
  programMemory.io.testerWriteEnable := io.testerProgMemWriteEnable
  // This signals are used by the tester for loading and dumping the data memory content, do not touch
  dataMemory.io.testerAddress := io.testerDataMemAddress
  io.testerDataMemDataRead := dataMemory.io.testerDataRead
  dataMemory.io.testerDataWrite := io.testerDataMemDataWrite
  dataMemory.io.testerEnable := io.testerDataMemEnable
  dataMemory.io.testerWriteEnable := io.testerDataMemWriteEnable
}
