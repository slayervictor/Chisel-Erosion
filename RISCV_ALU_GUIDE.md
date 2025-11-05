# RISC-V ALU Implementation Guide

## RISC-V ALU Operation Encoding

In RISC-V, ALU operations are determined by the **funct3** and **funct7** fields from the instruction:

```
R-Type Instruction Format:
┌─────────┬─────┬─────┬────────┬─────┬────────┐
│ funct7  │ rs2 │ rs1 │ funct3 │ rd  │ opcode │
│ [31:25] │[24:20][19:15][14:12][11:7] [6:0]  │
└─────────┴─────┴─────┴────────┴─────┴────────┘
    7       5     5       3      5       7
```

For the ALU, you typically use:
- **funct3** (3 bits): Primary operation selector
- **funct7[5]** (1 bit): Secondary selector (e.g., ADD vs SUB, SRL vs SRA)

## RV32I ALU Operations

| funct3 | funct7[5] | Operation | Description |
|--------|-----------|-----------|-------------|
| 000    | 0         | ADD       | Addition |
| 000    | 1         | SUB       | Subtraction |
| 001    | 0         | SLL       | Shift Left Logical |
| 010    | 0         | SLT       | Set Less Than (signed) |
| 011    | 0         | SLTU      | Set Less Than Unsigned |
| 100    | 0         | XOR       | Bitwise XOR |
| 101    | 0         | SRL       | Shift Right Logical |
| 101    | 1         | SRA       | Shift Right Arithmetic |
| 110    | 0         | OR        | Bitwise OR |
| 111    | 0         | AND       | Bitwise AND |

## ALU Interface Design

```scala
class ALU extends Module {
  val io = IO(new Bundle {
    val operandA = Input(UInt(32.W))          // rs1 value
    val operandB = Input(UInt(32.W))          // rs2 value or immediate
    val aluControl = Input(UInt(4.W))         // Combined funct3 + funct7[5]
    val result = Output(UInt(32.W))
  })
}
```

### Encoding aluControl (4 bits)

You can pack the control signals as:
```
aluControl = { funct7[5], funct3[2:0] }
```

This gives you:
- `0000` (0.U) = ADD
- `1000` (8.U) = SUB
- `0001` (1.U) = SLL
- `0010` (2.U) = SLT
- `0011` (3.U) = SLTU
- `0100` (4.U) = XOR
- `0101` (5.U) = SRL
- `1101` (13.U) = SRA
- `0110` (6.U) = OR
- `0111` (7.U) = AND

## Key Implementation Details

### Arithmetic Right Shift (SRA)
```scala
// Need to treat as signed for arithmetic shift
val shiftResult = (io.operandA.asSInt >> io.operandB(4,0)).asUInt
```

### Set Less Than (SLT)
```scala
// Compare as signed, return 1 or 0
val slt = Mux(io.operandA.asSInt < io.operandB.asSInt, 1.U, 0.U)
```

### Set Less Than Unsigned (SLTU)
```scala
// Compare as unsigned, return 1 or 0
val sltu = Mux(io.operandA < io.operandB, 1.U, 0.U)
```

### Shifts - Only use lower 5 bits
```scala
// RISC-V specifies shift amount is lower 5 bits of operandB
io.operandA << io.operandB(4,0)    // Not full 32 bits!
io.operandA >> io.operandB(4,0)
```

## Example Structure

```scala
import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val aluControl = Input(UInt(4.W))
    val result = Output(UInt(32.W))
  })

  // Extract control signals
  val funct3 = io.aluControl(2, 0)
  val funct7_5 = io.aluControl(3)

  // Default
  io.result := 0.U

  switch(funct3) {
    is("b000".U) {  // ADD or SUB
      when(funct7_5 === 0.U) {
        io.result := io.operandA + io.operandB        // ADD
      }.otherwise {
        io.result := io.operandA - io.operandB        // SUB
      }
    }
    is("b001".U) {  // SLL
      io.result := io.operandA << io.operandB(4, 0)
    }
    is("b010".U) {  // SLT
      io.result := Mux(io.operandA.asSInt < io.operandB.asSInt, 1.U, 0.U)
    }
    // ... implement the rest
  }
}
```

## Testing Your RISC-V ALU

```scala
// Test ADD
dut.io.operandA.poke(15.U)
dut.io.operandB.poke(10.U)
dut.io.aluControl.poke("b0000".U)  // funct7[5]=0, funct3=000
dut.io.result.expect(25.U)

// Test SUB
dut.io.aluControl.poke("b1000".U)  // funct7[5]=1, funct3=000
dut.io.result.expect(5.U)

// Test SLT (signed)
dut.io.operandA.poke("hFFFFFFFF".U)  // -1 in signed
dut.io.operandB.poke(1.U)
dut.io.aluControl.poke("b0010".U)     // funct3=010
dut.io.result.expect(1.U)             // -1 < 1 = true

// Test SLTU (unsigned)
dut.io.operandA.poke("hFFFFFFFF".U)   // Large unsigned
dut.io.operandB.poke(1.U)
dut.io.aluControl.poke("b0011".U)     // funct3=011
dut.io.result.expect(0.U)             // 0xFFFFFFFF > 1 = false

// Test shift
dut.io.operandA.poke(8.U)
dut.io.operandB.poke(2.U)
dut.io.aluControl.poke("b0001".U)     // SLL
dut.io.result.expect(32.U)            // 8 << 2 = 32
```

## Control Unit Integration

Your Control Unit will decode instructions and generate `aluControl`:

```scala
// In ControlUnit.scala
val funct3 = instruction(14, 12)
val funct7 = instruction(31, 25)

// Generate ALU control signal
when(opcode === "b0110011".U) {  // R-type
  io.aluControl := Cat(funct7(5), funct3)
}.elsewhen(opcode === "b0010011".U) {  // I-type (immediate)
  // For immediate ops, some instructions treat funct7[5] differently
  io.aluControl := Cat(0.U(1.W), funct3)  // Most use 0 for funct7[5]
}
```

## Quick Reference

| Operation | Binary    | Hex | Decimal |
|-----------|-----------|-----|---------|
| ADD       | 0000      | 0x0 | 0       |
| SUB       | 1000      | 0x8 | 8       |
| SLL       | 0001      | 0x1 | 1       |
| SLT       | 0010      | 0x2 | 2       |
| SLTU      | 0011      | 0x3 | 3       |
| XOR       | 0100      | 0x4 | 4       |
| SRL       | 0101      | 0x5 | 5       |
| SRA       | 1101      | 0xD | 13      |
| OR        | 0110      | 0x6 | 6       |
| AND       | 0111      | 0x7 | 7       |

---

Now implement the remaining operations following this pattern!
