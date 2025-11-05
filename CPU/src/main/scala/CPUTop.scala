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
  programCounter.io.run := io.run
  programMemory.io.address := programCounter.io.programCounter

  ////////////////////////////////////////////
  // Continue here with your connections
  ////////////////////////////////////////////

  val instruction = Wire(UInt(32.W))
  instruction := programMemory.io.instructionRead

  val pc = programCounter.io.programCounter
  val opcode = instruction(6, 0)
  val rd = instruction(11, 7)
  val funct3 = instruction(14, 12)
  val rs1 = instruction(19, 15)
  val rs2 = instruction(24, 20)
  val funct7 = instruction(31, 25)

  // check if it's immediate, not really used just here for cmpleteness
  val immI = Cat(Fill(20, instruction(31)), instruction(31, 20))
  val immS = Cat(instruction(31, 25), instruction(11, 7))

  controlUnit.io.opcode := opcode
  controlUnit.io.funct3 := funct3
  controlUnit.io.funct7 := funct7

  registerFile.io.aSel := rs1
  registerFile.io.bSel := rs2

  alu.io.operandA := registerFile.io.a
  alu.io.operandB := Mux(controlUnit.io.aluSrc, immI, registerFile.io.b)
  alu.io.funct7 := controlUnit.io.funct7
  alu.io.funct3 := controlUnit.io.funct3

  // Step 6: Memory Access
  dataMemory.io.address := alu.io.result(15, 0)
  dataMemory.io.dataWrite := registerFile.io.b
  dataMemory.io.writeEnable := controlUnit.io.memWrite

  // Step 7: Write Back
  val writeBackData =
    Mux(controlUnit.io.memToReg, dataMemory.io.dataRead, alu.io.result)
  registerFile.io.writeData := writeBackData
  registerFile.io.writeSel := rd
  registerFile.io.writeEnable := controlUnit.io.regWrite

  ////////////////////////////////////////////
  // Branch Logic: BEQ, BNE, BEQZ, BLT, J
  ////////////////////////////////////////////

  // Branch offset calculation (B-type format for BEQ, BNE, BLT, BEQZ)
  val branchOffset = Cat(
    Fill(4, instruction(31)), // Sign extend (bits 31-13)
    instruction(31), // bit 12
    instruction(7), // bit 11
    instruction(30, 25), // bits 10:5
    instruction(11, 8), // bits 4:1
    0.U(1.W) // bit 0 (always 0)
  ).asSInt.asUInt

  // Jump offset calculation (J-type format for J/JAL)
  val jumpOffset = Cat(
    Fill(12, instruction(31)), // Sign extend
    instruction(19, 12), // bits 19:12
    instruction(20), // bit 11
    instruction(30, 21), // bits 10:1
    0.U(1.W) // bit 0 (always 0)
  ).asSInt.asUInt

  // Calculate target addresses
  val branchTarget = (pc.asSInt + branchOffset.asSInt).asUInt
  val jumpTarget = (pc.asSInt + jumpOffset.asSInt).asUInt

  // Get register values for comparison
  val regAData = registerFile.io.a
  val regBData = registerFile.io.b

  // Branch condition evaluation
  val beqCond = (regAData === regBData) // BEQ: rs1 == rs2
  val bneCond = (regAData =/= regBData) // BNE: rs1 != rs2
  val bltCond = (regAData.asSInt < regBData.asSInt) // BLT: rs1 < rs2 (signed)

  // Determine which branch type based on funct3
  val isBEQ = (funct3 === "b000".U)
  val isBNE = (funct3 === "b001".U)
  val isBLT = (funct3 === "b100".U)

  val branchConditionMet =
    (isBEQ && beqCond) ||
      (isBNE && bneCond) ||
      (isBLT && bltCond)

  // Determine if we should jump
  val isJump = (opcode === "b1101111".U) // JAL/J opcode
  val takeBranch = (controlUnit.io.branch && branchConditionMet) || isJump

  // Select target address (branch vs jump)
  val jumpAddress = Mux(isJump, jumpTarget, branchTarget)

  // Connect to Program Counter
  programCounter.io.jump := takeBranch
  programCounter.io.programCounterJump := jumpAddress(
    15,
    0
  ) // Take lower 16 bits

  // Handle ECALL (done signal)
  val isECALL = (opcode === "b1110011".U) && (funct3 === "b000".U)
  io.done := isECALL
  programCounter.io.stop := isECALL

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
