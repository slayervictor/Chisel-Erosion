# Chisel-Erosion
## About

This is for assignment 2 of the course *Computer Systems (02132)* at the Technical University of Denmark. The CPU designed in this project is designed from scratch with some inspiration from the RISC-V architecture.

## Architecture

### Registers
- **8 general-purpose registers** (R0-R7)
- **32-bit wide** registers

### Data Path
- **Instruction width**: 32 bits
- **ALU operations**: 16-bit data path

### Instruction Format
```
Bits [31:16] - Immediate value (16-bit)
Bits [15:12] - Register 3 (4-bit)
Bits [11:8]  - Register 2 (4-bit)
Bits [7:4]   - Register 1 (4-bit)
Bits [3:0]   - Opcode (4-bit)
```

## Erosion
To run the Erosion algorithm, enter the CPU directory via `cd CPU` and run the CPUTopTester with the following command
```bash
sbt "testOnly CPUTopTester"
```

## Hardware Tests
To run all the tests, including the tests of the ALU, Register File and Program Counter, you can run the command.
```Bash
sbt test
```
This will also run the CPUTopTester which runs the Erosion program.

## Software
The Erosion program can be found in the `Erosion` directory, since the code is assembled by hand, there is no streamlined way of writing software for this architecture.

## Limitations
The CPU has the following constraints:
- **8 registers only** (R0-R7)
- **No subtraction instruction** (use ADDI with negative immediate values)
- **No division instruction**
- **16-bit immediate values** (range: -32768 to 32767)
- **No floating-point support**
- Basic logical and arithmetic operations only

## Instruction Set
| Opcode | Binary | Instruction | Operation | Example |
|--------|--------|-------------|-----------|---------|
| 0 | 0000 | NOP | No operation | `0x00000000` |
| 1 | 0001 | ADD | reg1 = reg2 + reg3 | `0x00012301` (R3=R1+R2) |
| 2 | 0010 | ADDI | reg1 = reg2 + imm | `0x000A1002` (R1=R1+10) |
| 3 | 0011 | LI | reg1 = imm | `0x00FF1003` (R1=255) |
| 4 | 0100 | LB | reg1 = memory[reg2] | `0x00002104` (R1=mem[R2]) |
| 5 | 0101 | SB | memory[reg2] = reg1 | `0x00002105` (mem[R2]=R1) |
| 6 | 0110 | BRANCH | if reg1 == 0, PC = imm | `0x00050026` (if R2==0, jump to 5) |
| 7 | 0111 | J | PC = imm | `0x000A0007` (jump to addr 10) |
| 8 | 1000 | EXIT | Halt execution | `0x00000008` |
| 9 | 1001 | MUL | reg1 = reg2 * reg3 | `0x00012309` (R3=R1*R2) |
| 10 | 1010 | OR | reg1 = reg2 OR reg3 | `0x0001230A` (R3=R1\|R2) |
| 11 | 1011 | AND | reg1 = reg2 AND reg3 | `0x0001230B` (R3=R1&R2) |
| 12 | 1100 | NOT | reg1 = NOT(reg2) | `0x0000210C` (R1=~R2) |

## Authors
- Victor Reynolds - [@slayervictor](https://github.com/slayervictor)
- Sebastian F. Taylor - [@Sebastian-Francis-Taylor](https://github.com/Sebastian-Francis-Taylor)

## License
This project is licensed under [LICENSE](LICENSE).
