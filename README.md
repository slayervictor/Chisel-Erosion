# Chisel-Erosion
## About

This is for assignment 2 of the course 'Computer Systems (02132)' at DTU. The cpu designed in this project is based of the RISC-V instruction set architecture.

## Erosion
To run the Erosion algorithm, enter the CPU via `cd CPU` and run the CPUTopTester with the following command
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

The Erosion program, along with an assembler built with python can be found in the `Erosion` directory, since our cpu is RISC-V compliant, our assembly code was written in RISC-V assembly.
