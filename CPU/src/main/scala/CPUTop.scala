import chisel3._
import chisel3.util._

class CPUTop extends Module {
  val io = IO(new Bundle {
    val done = Output(Bool())
    val run = Input(Bool())

    // Tester memory interfaces (do not touch)
    val testerDataMemEnable = Input(Bool())
    val testerDataMemAddress = Input(UInt(16.W))
    val testerDataMemDataRead = Output(UInt(32.W))
    val testerDataMemWriteEnable = Input(Bool())
    val testerDataMemDataWrite = Input(UInt(32.W))

    val testerProgMemEnable = Input(Bool())
    val testerProgMemAddress = Input(UInt(16.W))
    val testerProgMemDataRead = Output(UInt(32.W))
    val testerProgMemWriteEnable = Input(Bool())
    val testerProgMemDataWrite = Input(UInt(32.W))
  })

  // connecting components
  val programCounter = Module(new ProgramCounter())
  val dataMemory = Module(new DataMemory())
  val programMemory = Module(new ProgramMemory())
  val registerFile = Module(new RegisterFile())
  val controlUnit = Module(new ControlUnit())
  val alu = Module(new ALU())

  // default
  io.done := false.B
  programCounter.io.run := io.run
  programCounter.io.jump := false.B
  programCounter.io.stop := false.B
  programCounter.io.programCounterJump := 0.U

  // memory
  programMemory.io.address := programCounter.io.programCounter
  controlUnit.io.instruction := programMemory.io.instructionRead

  // alu
  alu.io.aSel := 0.U
  alu.io.bSel := 0.U
  alu.io.func := 0.U

  // memory
  dataMemory.io.address := 0.U
  dataMemory.io.writeEnable := false.B
  dataMemory.io.dataWrite := 0.U

  // register file
  registerFile.io.aSel := 0.U
  registerFile.io.bSel := 0.U
  registerFile.io.writeData := 0.U
  registerFile.io.writeSel := 0.U
  registerFile.io.writeEnable := false.B

  // instructions
  when(controlUnit.io.nop) {
    // Do nothing

  }.elsewhen(controlUnit.io.add) {
    alu.io.func := 0.U // add
    registerFile.io.aSel := controlUnit.io.reg2
    registerFile.io.bSel := controlUnit.io.reg3
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := registerFile.io.b
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result

  }.elsewhen(controlUnit.io.addi) {
    alu.io.func := 0.U // add
    registerFile.io.aSel := controlUnit.io.reg2
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := controlUnit.io.imm
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result

  }.elsewhen(controlUnit.io.mul) {
    alu.io.func := 1.U // mult
    registerFile.io.aSel := controlUnit.io.reg2
    registerFile.io.bSel := controlUnit.io.reg3
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := registerFile.io.b
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result

  }.elsewhen(controlUnit.io.or) {
    alu.io.func := 2.U // or
    registerFile.io.aSel := controlUnit.io.reg2
    registerFile.io.bSel := controlUnit.io.reg3
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := registerFile.io.b
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result

  }.elsewhen(controlUnit.io.and) {
    alu.io.func := 3.U // and
    registerFile.io.aSel := controlUnit.io.reg2
    registerFile.io.bSel := controlUnit.io.reg3
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := registerFile.io.b
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result

  }.elsewhen(controlUnit.io.not) {
    alu.io.func := 4.U // not b
    registerFile.io.bSel := controlUnit.io.reg2
    alu.io.bSel := registerFile.io.b
    alu.io.aSel := 0.U // not ignores a
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := alu.io.result

  }.elsewhen(controlUnit.io.lb) {
    alu.io.func := 0.U // add (address = reg2 + imm)
    registerFile.io.aSel := controlUnit.io.reg2
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := controlUnit.io.imm
    dataMemory.io.address := alu.io.result
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := dataMemory.io.dataRead

  }.elsewhen(controlUnit.io.sb) {
    alu.io.func := 0.U // add (address = reg2 + imm)
    registerFile.io.aSel := controlUnit.io.reg2
    alu.io.aSel := registerFile.io.a
    alu.io.bSel := controlUnit.io.imm
    dataMemory.io.address := alu.io.result
    dataMemory.io.writeEnable := true.B
    registerFile.io.bSel := controlUnit.io.reg1
    dataMemory.io.dataWrite := registerFile.io.b

  }.elsewhen(controlUnit.io.li) {
    registerFile.io.writeEnable := true.B
    registerFile.io.writeSel := controlUnit.io.reg1
    registerFile.io.writeData := controlUnit.io.imm

  }.elsewhen(controlUnit.io.branch) {
    alu.io.func := 5.U // isZero
    registerFile.io.aSel := controlUnit.io.reg1
    alu.io.aSel := registerFile.io.a
    when(alu.io.result === 1.U) {
      programCounter.io.jump := true.B
      programCounter.io.programCounterJump := controlUnit.io.imm
    }

  }.elsewhen(controlUnit.io.j) {
    programCounter.io.jump := true.B
    programCounter.io.programCounterJump := controlUnit.io.imm

  }.elsewhen(controlUnit.io.exit) {
    io.done := true.B
    programCounter.io.stop := true.B
  }

// dont touch
  programMemory.io.testerAddress := io.testerProgMemAddress
  io.testerProgMemDataRead := programMemory.io.testerDataRead
  programMemory.io.testerDataWrite := io.testerProgMemDataWrite
  programMemory.io.testerEnable := io.testerProgMemEnable
  programMemory.io.testerWriteEnable := io.testerProgMemWriteEnable

  dataMemory.io.testerAddress := io.testerDataMemAddress
  io.testerDataMemDataRead := dataMemory.io.testerDataRead
  dataMemory.io.testerDataWrite := io.testerDataMemDataWrite
  dataMemory.io.testerEnable := io.testerDataMemEnable
  dataMemory.io.testerWriteEnable := io.testerDataMemWriteEnable
}
