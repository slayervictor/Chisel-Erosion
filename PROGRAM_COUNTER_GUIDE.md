# Program Counter Guide

## Table of Contents
1. [What is a Program Counter?](#what-is-a-program-counter)
2. [Your Implementation](#your-implementation)
3. [Understanding the Interface](#understanding-the-interface)
4. [How It Works](#how-it-works)
5. [Testing Your Program Counter](#testing-your-program-counter)
6. [Visualizing with Waveforms](#visualizing-with-waveforms)
7. [Integration with CPU](#integration-with-cpu)
8. [Next Steps](#next-steps)

---

## What is a Program Counter?

The **Program Counter (PC)** is one of the most fundamental components in a CPU. Think of it as the CPU's "instruction pointer" that keeps track of which instruction should be executed next.

### Key Responsibilities:
- **Points to the next instruction** in program memory
- **Increments automatically** after each instruction (sequential execution)
- **Can jump** to different addresses for branches, loops, and function calls
- **Resets** when the CPU starts

In your RISC-V CPU project, the PC uses a **16-bit address space**, meaning it can point to any of 65,536 different memory locations.

---

## Your Implementation

**File Location:** `CPU/src/main/scala/ProgramCounter.scala`

Your program counter is a complete, tested Chisel module with the following features:

### Hardware Components:
```scala
val counterReg = RegInit(0.U(16.W))
```
- **16-bit register** initialized to 0 at reset
- Holds the current program counter value
- Updates on every clock cycle based on control signals

### State Machine Behavior:

```
┌─────────────────────────────────────┐
│   Priority: jump > increment > hold │
└─────────────────────────────────────┘

         jump = 1?
            │
        ┌───┴───┐
       YES     NO
        │       │
    Load New   run=1 AND stop=0?
    Address     │
        │   ┌───┴───┐
        │  YES     NO
        │   │       │
        │  PC++    Hold
        │   │       │
        └───┴───┴───┘
             │
        Output PC
```

---

## Understanding the Interface

### Input Signals

| Signal | Type | Width | Purpose |
|--------|------|-------|---------|
| `stop` | Input | 1-bit | Halt incrementing (like a pause button) |
| `jump` | Input | 1-bit | Enable jumping to a new address |
| `run` | Input | 1-bit | Enable the program counter to run |
| `programCounterJump` | Input | 16-bit | Target address for jump operations |

### Output Signals

| Signal | Type | Width | Purpose |
|--------|------|-------|---------|
| `programCounter` | Output | 16-bit | Current PC value (instruction address) |

### Control Logic Priority:

1. **HIGHEST:** `jump=1` → Load `programCounterJump` value immediately
2. **MIDDLE:** `run=1 AND stop=0` → Increment by 1
3. **LOWEST:** Otherwise → Hold current value

### Example Scenarios:

```scala
// Scenario 1: Normal execution
jump=0, run=1, stop=0  →  PC = PC + 1  (increment each cycle)

// Scenario 2: Jump to address 100
jump=1, programCounterJump=100  →  PC = 100  (immediate load)

// Scenario 3: Pause execution
jump=0, run=1, stop=1  →  PC = PC  (no change)

// Scenario 4: Not running
jump=0, run=0  →  PC = PC  (no change)
```

---

## How It Works

### The Chisel Code Breakdown

**Location:** `ProgramCounter.scala:17-21`

```scala
when(io.jump) {
  counterReg := io.programCounterJump     // Priority 1: Load new address
} .elsewhen(io.run && !io.stop) {
  counterReg := counterReg + 1.U          // Priority 2: Increment
}                                         // Priority 3: Hold (implicit)

io.programCounter := counterReg           // Always output current value
```

### Clock-by-Clock Execution Example:

```
Cycle | jump | run | stop | jumpAddr | PC Output | Explanation
------|------|-----|------|----------|-----------|---------------------------
  0   |  0   |  1  |  0   |   -      |    0      | Reset state
  1   |  0   |  1  |  0   |   -      |    1      | Increment (0 + 1)
  2   |  0   |  1  |  0   |   -      |    2      | Increment (1 + 1)
  3   |  0   |  1  |  1   |   -      |    2      | Stopped (held at 2)
  4   |  1   |  1  |  0   |   50     |    50     | Jump to address 50
  5   |  0   |  1  |  0   |   -      |    51     | Resume incrementing
```

### Why This Design?

- **Jump has highest priority** because branch instructions need immediate effect
- **Run and stop work together** to control execution flow (useful for debugging)
- **Default hold behavior** is safe and prevents unwanted changes
- **16-bit width** matches your program memory address space (64K)

---

## Testing Your Program Counter

### Running the Tests

Your test suite is already complete and passing!

**Test File:** `CPU/src/test/scala/ProgramCounterTester.scala`

```bash
# Navigate to the CPU directory
cd CPU

# Run the program counter test
sbt "testOnly ProgramCounterTester"
```

### What the Test Does

The test validates all three operational modes:

#### Phase 1: Normal Increment (5 cycles)
```scala
jump=false, run=true, stop=false
Expected: PC goes 0 → 1 → 2 → 3 → 4 → 5
```

#### Phase 2: Stop While Running (5 cycles)
```scala
jump=false, run=true, stop=true
Expected: PC stays at 5 for all 5 cycles
```

#### Phase 3: Not Running (5 cycles)
```scala
jump=false, run=false, stop=false
Expected: PC stays at 5 for all 5 cycles
```

#### Phase 4: Jump to Address 30 (1 cycle)
```scala
jump=true, programCounterJump=30
Expected: PC immediately becomes 30
```

#### Phase 5: Resume Increment (5 cycles)
```scala
jump=false, run=true, stop=false
Expected: PC goes 30 → 31 → 32 → 33 → 34 → 35
```

### Expected Output

```
[info] ProgramCounterTester:
[info] - ProgramCounterTester should pass
[info] Run completed in X seconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
```

---

## Visualizing with Waveforms

The test generates a **VCD (Value Change Dump)** file that you can view with a waveform viewer.

### Generated File Location:
```
CPU/test_run_dir/ProgramCounterTester_should_pass/ProgramCounter.vcd
```

### Viewing with GTKWave:

```bash
# Install GTKWave (if not already installed)
# On Arch Linux:
sudo pacman -S gtkwave

# Open the waveform
cd CPU
gtkwave test_run_dir/ProgramCounterTester_should_pass/ProgramCounter.vcd
```

### What to Look For:

In GTKWave, add these signals to observe behavior:
- `clock` - System clock
- `reset` - Reset signal
- `io_jump` - Jump enable
- `io_run` - Run enable
- `io_stop` - Stop signal
- `io_programCounterJump` - Jump target address
- `io_programCounter` - **Current PC output** (the main signal)
- `counterReg` - Internal register state

You should see:
- PC incrementing from 0→5 in the first 5 cycles
- PC holding at 5 for the next 10 cycles
- PC jumping to 30
- PC incrementing from 30→35

---

## Integration with CPU

Your program counter is designed to integrate with the larger CPU system in `CPUTop.scala`.

### Current Status:

The connections are **commented out** and waiting for you to enable them:

**File:** `CPU/src/main/scala/CPUTop.scala:31-32`

```scala
//Connecting the modules
//programCounter.io.run := io.run
//programMemory.io.address := programCounter.io.programCounter
```

### What These Connections Do:

```
┌─────────────────┐
│   CPUTop.io.run │  ─────────────────┐
└─────────────────┘                   │
                                      ▼
                           ┌──────────────────────┐
                           │   programCounter     │
                           │  .io.run = true      │
                           │                      │
                           │  .io.programCounter  │──┐
                           └──────────────────────┘  │
                                                      │
                                                      ▼
                           ┌──────────────────────┐
                           │   programMemory      │
                           │  .io.address ← PC    │
                           │                      │
                           │  Fetches instruction │
                           └──────────────────────┘
```

### Integration Checklist:

When you're ready to integrate the PC into your CPU:

- [ ] **Uncomment line 31** to connect the run signal from CPU top to PC
- [ ] **Uncomment line 32** to connect PC output to program memory address
- [ ] **Add stop signal connection** from control unit:
  ```scala
  programCounter.io.stop := controlUnit.io.stopPC
  ```
- [ ] **Add jump signal connection** from control unit:
  ```scala
  programCounter.io.jump := controlUnit.io.jumpEnable
  programCounter.io.programCounterJump := controlUnit.io.jumpTarget
  ```

### Full Integration Pattern:

```scala
// In CPUTop.scala after implementing ControlUnit:

programCounter.io.run := io.run
programCounter.io.stop := controlUnit.io.halt
programCounter.io.jump := controlUnit.io.branch
programCounter.io.programCounterJump := alu.io.branchTarget

programMemory.io.address := programCounter.io.programCounter
```

---

## Next Steps

Your program counter is complete and tested! Here's what to work on next:

### 1. Implement the Control Unit (Priority: HIGH)

The control unit will generate the `stop` and `jump` signals for your PC.

**File:** `CPU/src/main/scala/ControlUnit.scala`

**Tasks:**
- [ ] Decode instruction opcodes (RISC-V format)
- [ ] Generate control signals for:
  - Branch instructions (BEQ, BNE, BLT, etc.)
  - Jump instructions (JAL, JALR)
  - Halt/stop conditions
- [ ] Connect to PC's `jump` and `stop` inputs

### 2. Connect PC to Program Memory (Priority: HIGH)

Once you're confident in the PC, enable the connection:

**File:** `CPU/src/main/scala/CPUTop.scala:32`

```scala
programMemory.io.address := programCounter.io.programCounter
```

This allows your PC to fetch instructions from memory.

### 3. Implement the ALU (Priority: MEDIUM)

**File:** `CPU/src/main/scala/ALU.scala`

The ALU will:
- [ ] Perform arithmetic operations (ADD, SUB, etc.)
- [ ] Calculate branch target addresses
- [ ] Provide results to control unit for conditional branches

### 4. Implement the Register File (Priority: MEDIUM)

**File:** `CPU/src/main/scala/RegisterFile.scala`

Note: Your PC currently has a register file in its interface (line 11):
```scala
val regFile = Mem(32, UInt(32.W))
```

**This should probably be removed from PC and moved to the dedicated RegisterFile module!**

### 5. Test Integration

After connecting everything:
- [ ] Run `CPUTopTester` to test full system
- [ ] Load a simple RISC-V program
- [ ] Verify PC increments through the program
- [ ] Test branch and jump instructions
- [ ] Verify stop condition halts the PC

---

## Quick Reference

### Common Tasks:

**Run tests:**
```bash
cd CPU
sbt test                              # Run all tests
sbt "testOnly ProgramCounterTester"  # Run PC test only
```

**Generate Verilog:**
```bash
cd CPU
sbt "runMain circt.stage.ChiselMain --module ProgramCounter --target-dir generated"
```

**Clean build artifacts:**
```bash
cd CPU
sbt clean
```

### Key Files:

| File | Purpose |
|------|---------|
| `CPU/src/main/scala/ProgramCounter.scala` | PC implementation |
| `CPU/src/test/scala/ProgramCounterTester.scala` | PC tests |
| `CPU/src/main/scala/CPUTop.scala` | Top-level integration |
| `CPU/build.sbt` | Build configuration |

### Useful Resources:

- **Chisel Documentation:** https://www.chisel-lang.org/
- **RISC-V ISA Spec:** https://riscv.org/specifications/
- **Chisel Bootcamp:** https://github.com/freechipsproject/chisel-bootcamp

---

## Troubleshooting

### Test Fails

If your test fails, check:
1. Did you modify `ProgramCounter.scala` since it was working?
2. Is the priority correct? (jump > increment > hold)
3. Are all signals properly connected in the test?

### Waveform Not Generated

If no VCD file appears:
1. Ensure `.withAnnotations(Seq(WriteVcdAnnotation))` is in the test
2. Check `test_run_dir/` for the subdirectory
3. Try `sbt clean` then rerun tests

### Integration Issues

When connecting to CPUTop:
1. Verify signal widths match (all should be consistent)
2. Check that clock and reset are properly connected
3. Ensure tester signals aren't conflicting with normal operation

---

## Summary

Your program counter is a solid implementation with:
- Three clear operational modes (jump, increment, hold)
- Proper priority handling
- Complete test coverage
- 16-bit address space for 64K memory
- Ready for CPU integration

The next major milestone is implementing the Control Unit to generate the jump and stop signals dynamically based on decoded instructions.

Happy hardware designing!
