# ALU Chisel Syntax Quick Reference

## Basic IO Bundle Structure

```scala
class ALU extends Module {
  val io = IO(new Bundle {
    val operandA = Input(UInt(32.W))
    val operandB = Input(UInt(32.W))
    val operation = Input(UInt(4.W))      // Opcode to select operation
    val result = Output(UInt(32.W))
    val zero = Output(Bool())              // Common flag: result == 0
  })
```

## Operation Selection Patterns

### Pattern 1: switch/is (Cleanest for many operations)

```scala
import chisel3.util.switch
import chisel3.util.is

switch(io.operation) {
  is(0.U) { io.result := io.operandA + io.operandB }
  is(1.U) { io.result := io.operandA - io.operandB }
  is(2.U) { io.result := io.operandA & io.operandB }
  is(3.U) { io.result := io.operandA | io.operandB }
}
```

### Pattern 2: when/elsewhen (Good for priorities)

```scala
when(io.operation === 0.U) {
  io.result := io.operandA + io.operandB
}.elsewhen(io.operation === 1.U) {
  io.result := io.operandA - io.operandB
}.elsewhen(io.operation === 2.U) {
  io.result := io.operandA & io.operandB
}.otherwise {
  io.result := 0.U
}
```

### Pattern 3: MuxCase (Functional style)

```scala
import chisel3.util.MuxCase

io.result := MuxCase(0.U, Array(
  (io.operation === 0.U) -> (io.operandA + io.operandB),
  (io.operation === 1.U) -> (io.operandA - io.operandB),
  (io.operation === 2.U) -> (io.operandA & io.operandB)
))
```

## Chisel Arithmetic & Logic Operators

### Arithmetic
```scala
val sum = a + b                    // Addition
val diff = a - b                   // Subtraction
val product = a * b                // Multiplication
val quotient = a / b               // Division
val remainder = a % b              // Modulo
```

### Bitwise Logic
```scala
val andResult = a & b              // Bitwise AND
val orResult = a | b               // Bitwise OR
val xorResult = a ^ b              // Bitwise XOR
val notResult = ~a                 // Bitwise NOT
```

### Shifts
```scala
val leftShift = a << b             // Logical left shift
val rightShift = a >> b            // Logical right shift (zero-fill)
val arithShift = (a.asSInt >> b).asUInt  // Arithmetic right shift (sign-extend)
```

### Comparisons (Return Bool)
```scala
val equal = a === b                // Equal
val notEqual = a =/= b             // Not equal
val lessThan = a < b               // Less than (unsigned)
val greaterThan = a > b            // Greater than (unsigned)
val lessEqual = a <= b             // Less than or equal
val greaterEqual = a >= b          // Greater than or equal
```

### Signed Comparisons
```scala
val asSigned = a.asSInt
val bSigned = b.asSInt
val signedLess = asSigned < bSigned
```

## Common Patterns

### Conditional Result (Mux)
```scala
val result = Mux(condition, valueIfTrue, valueIfFalse)

// Example: absolute value
val absValue = Mux(signedNum < 0.S, -signedNum, signedNum)
```

### Zero Flag
```scala
io.zero := io.result === 0.U
```

### Sign Extension
```scala
val extended = operand(15, 0).asSInt.pad(32).asUInt  // Sign-extend 16-bit to 32-bit
```

### Width Conversion
```scala
val widened = operand.pad(64)           // Zero-extend to 64 bits
val truncated = operand(15, 0)          // Take lower 16 bits
val specific = operand(23, 16)          // Extract bits 23-16
```

### Concatenation
```scala
val combined = Cat(upperBits, lowerBits)
```

### Fill (Repeat bit)
```scala
val allOnes = Fill(32, 1.U)            // 32-bit all 1s
val signExtend = Fill(16, bit) ## lower16Bits
```

## Wire vs Direct Assignment

### Using Wire (when you need intermediate values)
```scala
val temp = Wire(UInt(32.W))
temp := io.operandA + io.operandB
io.result := temp
```

### Direct Assignment (simpler when possible)
```scala
io.result := io.operandA + io.operandB
```

## Default Values Pattern

```scala
// Set default to avoid latches
io.result := 0.U
io.zero := false.B

// Then override in your logic
switch(io.operation) {
  is(0.U) { io.result := io.operandA + io.operandB }
  // ...
}

// Update flags after result is computed
io.zero := io.result === 0.U
```

## Example: Simple 4-Operation ALU

```scala
import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(32.W))
    val b = Input(UInt(32.W))
    val op = Input(UInt(2.W))
    val result = Output(UInt(32.W))
  })

  // Default value
  io.result := 0.U

  switch(io.op) {
    is(0.U) { io.result := io.a + io.b }      // ADD
    is(1.U) { io.result := io.a - io.b }      // SUB
    is(2.U) { io.result := io.a & io.b }      // AND
    is(3.U) { io.result := io.a | io.b }      // OR
  }
}
```

## Testing Quick Reference

```scala
// In your tester:
dut.io.a.poke(10.U)
dut.io.b.poke(5.U)
dut.io.op.poke(0.U)          // ADD
dut.clock.step(1)
dut.io.result.expect(15.U)

dut.io.op.poke(1.U)          // SUB
dut.clock.step(1)
dut.io.result.expect(5.U)
```

## Gotchas

1. **Use `:=` for assignment**, not `=`
2. **Use `===` for comparison**, not `==`
3. **Unsigned by default**: Use `.asSInt` for signed operations
4. **Width inference**: Chisel infers widths, but be explicit when needed
5. **No latches**: Always assign all outputs in all code paths (use defaults)

## Common ALU Flags

```scala
val io = IO(new Bundle {
  // ... operands and operation ...
  val result = Output(UInt(32.W))
  val zero = Output(Bool())              // Z: result is zero
  val negative = Output(Bool())          // N: result is negative (MSB)
  val carry = Output(Bool())             // C: carry out from addition
  val overflow = Output(Bool())          // V: signed overflow
})

// Flag implementations
io.zero := io.result === 0.U
io.negative := io.result(31)

// For carry (need wider addition)
val sum = io.a +& io.b                   // +& gives result with extra bit
io.carry := sum(32)

// For overflow (signed)
val aSign = io.a(31)
val bSign = io.b(31)
val resultSign = io.result(31)
io.overflow := (aSign === bSign) && (resultSign =/= aSign)
```

---

That's it! Pick your operation encoding scheme and implement your operations using these patterns.
