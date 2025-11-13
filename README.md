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
| Decimal | Instruction | Description | Binary |
|--------|-------------|-------------|--------|
| 1 | ADD | R1 = R2 + R3 | b00001 |
| 2 | SUB | R1 = R2 - R3 | b00010 |
| 3 | MULT | R1 = R2 * R3 | b00011 |
| 4 | ADDI | R1 = R2 + imm | b00100 |
| 5 | SUBI | R1 = R2 - imm | b00101 |
| 6 | OR | R1 = R2 OR R3 | b00110 |
| 7 | AND | R1 = R2 AND R3 | b00111 |
| 8 | NOT | R1 = NOT(R2) | b01000 |
| 9 | - | Reserved | b01001 |
| 10 | LI | R1 = imm | b01010 |
| 11 | LD | R1 = memory[R2] | b01011 |
| 12 | SD | memory[R2] = R1 | b01100 |
| 13 | - | Reserved | b01101 |
| 14 | JR | Jump to instruction | b01110 |
| 15 | JEQ | Jump if R2 == R3 | b01111 |
| 16 | JLT | Jump if R2 < R3 | b10000 |
| 17 | JGT | Jump if R2 > R3 | b10001 |
| 18 | NOP | No operation | b10010 |
| 19 | END | Terminate execution | b10011 |

## Authors
- Victor Reynolds - [@slayervictor](https://github.com/slayervictor)
- Sebastian F. Taylor - [@Sebastian-Francis-Taylor](https://github.com/Sebastian-Francis-Taylor)

## License
This project is licensed under [LICENSE](LICENSE).
