# Chisel-Erosion
## About

This is for assignment 2 of the course *Computer Systems (02132)* at the Technical University of Denmark. The CPU designed in this project is designed from scratch with some inspiration from the RISC-V architecutre.

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
The Erosion program can be found in the `Erosion` directory, since the code is assembled by hand, there is no streamlined way of writing software for this architecutre.

## limitations
The CPU Supports basic logical and arithmetic operations, but it is general purpose, so any software should run, assuming that the assembly is written with the limited scope in mind.

## Instruction Set
| Opcode | Binary | Instruction | Description |
|--------|--------|-------------|-------------|
| 0 | 0000 | NOP | No operation |
| 1 | 0001 | ADD | R1 = R2 + R3 |
| 2 | 0010 | ADDI | R1 = R2 + imm |
| 3 | 0011 | LI | R1 = imm (Load Immediate) |
| 4 | 0100 | LB | R1 = memory[R2] (Load Byte) |
| 5 | 0101 | SB | memory[R2] = R1 (Store Byte) |
| 6 | 0110 | BRANCH | Branch if R2 == 0 |
| 7 | 0111 | J | Jump to address in imm |
| 8 | 1000 | EXIT | Terminate execution |
| 9 | 1001 | MUL | R1 = R2 * R3 |
| 10 | 1010 | OR | R1 = R2 OR R3 |
| 11 | 1011 | AND | R1 = R2 AND R3 |
| 12 | 1100 | NOT | R1 = NOT(R2) |
| 13 | 1101 | JEQ | Jump if R2 == R3 |
| 14 | 1110 | JLT | Jump if R2 < R3 |
| 15 | 1111 | JGT | Jump if R2 > R3 |

## Authors
- Victor Reynolds - [@slayervictor](https://github.com/slayervictor)
- Sebastian F. Taylor - [@Sebastian-Francis-Taylor](https://github.com/Sebastian-Francis-Taylor)

## License
This project is licensed under [LICENSE](LICENSE).
