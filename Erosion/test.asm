# RISC-V Assembly Test Suite for Chisel CPU
# Target: CPU with 16 registers (x0-x15), limited instruction set
# 
# Supported Instructions:
# - R-type: ADD, SUB, MUL, AND, OR, XOR, SLL, SRL, SRA
# - I-type: ADDI, ANDI, ORI, XORI, SLLI, SRLI, SRAI
# - Load: LW
# - Store: SW
# - Branch: BEQ, BNE, BLT
# - Jump: JAL
# - System: ECALL

.data
    test_val1:  .word 15
    test_val2:  .word 7
    test_val3:  .word -3
    result1:    .word 0
    result2:    .word 0
    result3:    .word 0
    array:      .word 1, 2, 3, 4, 5

.text
.globl main

# ==============================================================================
# TEST 1: Basic Arithmetic (R-type and I-type)
# ==============================================================================
main:
    # Initialize registers with test values
    addi x1, x0, 15     # x1 = 15
    addi x2, x0, 7      # x2 = 7
    addi x3, x0, -3     # x3 = -3 (sign-extended)
    
    # Test ADD
    add x4, x1, x2      # x4 = 15 + 7 = 22
    
    # Test ADDI
    addi x5, x1, 10     # x5 = 15 + 10 = 25
    
    # Test SUB
    sub x6, x1, x2      # x6 = 15 - 7 = 8
    
    # Test MUL (funct7=0x01)
    # Note: Your ALU uses funct7=1 for MUL
    # mul x7, x1, x2    # x7 = 15 * 7 = 105
    
    # Test AND
    addi x8, x0, 0xFF   # x8 = 255 (0x000000FF)
    and x9, x1, x8      # x9 = 15 & 255 = 15
    
    # Test OR
    or x10, x1, x2      # x10 = 15 | 7 = 15
    
    # Test XOR
    xor x11, x1, x2     # x11 = 15 ^ 7 = 8
    
    # Test ANDI
    andi x12, x1, 0x0F  # x12 = 15 & 15 = 15
    
    # Test ORI
    ori x13, x1, 0x10   # x13 = 15 | 16 = 31
    
    # Test XORI
    xori x14, x1, 0xFF  # x14 = 15 ^ 255 = 240

# ==============================================================================
# TEST 2: Shift Operations
# ==============================================================================
test_shifts:
    addi x1, x0, 8      # x1 = 8 (0b1000)
    
    # Test Shift Left Logical (SLL)
    slli x2, x1, 2      # x2 = 8 << 2 = 32
    
    # Test Shift Right Logical (SRL)
    srli x3, x1, 1      # x3 = 8 >> 1 = 4
    
    # Test Shift Right Arithmetic (SRA) with negative number
    addi x4, x0, -8     # x4 = -8 (0xFFFFFFF8)
    srai x5, x4, 1      # x5 = -8 >> 1 = -4 (sign extended)
    
    # Test R-type shifts
    addi x6, x0, 2      # shift amount
    sll x7, x1, x6      # x7 = 8 << 2 = 32
    srl x8, x1, x6      # x8 = 8 >> 2 = 2

# ==============================================================================
# TEST 3: Memory Operations (Load/Store)
# ==============================================================================
test_memory:
    # Store values to memory
    addi x1, x0, 100    # x1 = 100
    addi x2, x0, 200    # x2 = 200
    
    # Assuming data memory starts at address 0
    sw x1, 0(x0)        # Store 100 at address 0
    sw x2, 4(x0)        # Store 200 at address 4
    
    # Load values back
    lw x3, 0(x0)        # x3 = 100 (from address 0)
    lw x4, 4(x0)        # x4 = 200 (from address 4)
    
    # Verify loaded correctly by adding
    add x5, x3, x4      # x5 = 100 + 200 = 300
    
    # Store result back
    sw x5, 8(x0)        # Store 300 at address 8

# ==============================================================================
# TEST 4: Branch Instructions
# ==============================================================================
test_branches:
    addi x1, x0, 10     # x1 = 10
    addi x2, x0, 10     # x2 = 10
    addi x3, x0, 5      # x3 = 5
    
    # Test BEQ (Branch if Equal)
    beq x1, x2, beq_taken   # Should branch (10 == 10)
    addi x4, x0, 99     # Should NOT execute
beq_taken:
    addi x4, x0, 1      # x4 = 1 (branch was taken)
    
    # Test BNE (Branch if Not Equal)
    bne x1, x3, bne_taken   # Should branch (10 != 5)
    addi x5, x0, 99     # Should NOT execute
bne_taken:
    addi x5, x0, 2      # x5 = 2 (branch was taken)
    
    # Test BLT (Branch if Less Than)
    blt x3, x1, blt_taken   # Should branch (5 < 10)
    addi x6, x0, 99     # Should NOT execute
blt_taken:
    addi x6, x0, 3      # x6 = 3 (branch was taken)
    
    # Test branch NOT taken
    beq x1, x3, skip_this   # Should NOT branch (10 != 5)
    addi x7, x0, 4      # x7 = 4 (branch not taken, correct)
skip_this:

# ==============================================================================
# TEST 5: Jump (JAL)
# ==============================================================================
test_jump:
    # JAL saves PC+4 to rd and jumps to target
    jal x8, jump_target     # x8 = return address, jump to target
    addi x9, x0, 99         # Should NOT execute
jump_target:
    addi x9, x0, 5          # x9 = 5
    # Note: Return address in x8 could be used with JALR (not supported)

# ==============================================================================
# TEST 6: Loop Example - Sum array
# ==============================================================================
test_loop:
    # Sum numbers 1 to 5
    addi x1, x0, 0      # sum = 0
    addi x2, x0, 1      # counter = 1
    addi x3, x0, 6      # limit = 6
    
loop_start:
    add x1, x1, x2      # sum += counter
    addi x2, x2, 1      # counter++
    blt x2, x3, loop_start  # if counter < 6, loop
    # x1 should now be 15 (1+2+3+4+5)

# ==============================================================================
# TEST 7: Conditional Logic - Find Maximum
# ==============================================================================
test_max:
    addi x1, x0, 25     # a = 25
    addi x2, x0, 17     # b = 17
    
    # Find max(a, b)
    sub x3, x1, x2      # x3 = a - b
    blt x1, x2, b_larger  # if a < b, jump
    # a >= b
    add x4, x0, x1      # max = a
    jal x0, max_done
b_larger:
    add x4, x0, x2      # max = b
max_done:
    # x4 now contains max(25, 17) = 25

# ==============================================================================
# TEST 8: Bitwise Operations Example
# ==============================================================================
test_bitwise:
    addi x1, x0, 0x0F   # x1 = 0x0000000F
    addi x2, x0, 0x33   # x2 = 0x00000033
    
    and x3, x1, x2      # x3 = 0x03
    or x4, x1, x2       # x4 = 0x3F
    xor x5, x1, x2      # x5 = 0x3C
    
    # Create mask and apply
    addi x6, x0, 0xFF   # mask = 0xFF
    and x7, x2, x6      # x7 = 0x33 & 0xFF = 0x33

# ==============================================================================
# TEST 9: Negative Numbers
# ==============================================================================
test_negative:
    addi x1, x0, -1     # x1 = -1 (0xFFFFFFFF)
    addi x2, x0, -10    # x2 = -10
    
    add x3, x1, x2      # x3 = -1 + (-10) = -11
    sub x4, x1, x2      # x4 = -1 - (-10) = 9
    
    # Test signed comparison
    addi x5, x0, 5      # x5 = 5
    blt x2, x5, neg_less  # -10 < 5, should branch
    addi x6, x0, 99
    jal x0, neg_done
neg_less:
    addi x6, x0, 1      # x6 = 1
neg_done:

# ==============================================================================
# TEST 10: Register Usage (Using all 16 registers x0-x15)
# ==============================================================================
test_registers:
    # x0 is always 0
    addi x1, x0, 1
    addi x2, x0, 2
    addi x3, x0, 3
    addi x4, x0, 4
    addi x5, x0, 5
    addi x6, x0, 6
    addi x7, x0, 7
    addi x8, x0, 8
    addi x9, x0, 9
    addi x10, x0, 10
    addi x11, x0, 11
    addi x12, x0, 12
    addi x13, x0, 13
    addi x14, x0, 14
    addi x15, x0, 15
    
    # Sum all registers
    add x1, x1, x2
    add x1, x1, x3
    add x1, x1, x4
    add x1, x1, x5
    add x1, x1, x6
    add x1, x1, x7
    add x1, x1, x8
    add x1, x1, x9
    add x1, x1, x10
    add x1, x1, x11
    add x1, x1, x12
    add x1, x1, x13
    add x1, x1, x14
    add x1, x1, x15
    # x1 should be 120 (sum of 1-15)

# ==============================================================================
# End of program - ECALL to terminate
# ==============================================================================
end_program:
    ecall               # Terminate (sets done signal)

# ==============================================================================
# SIMPLE TEST PROGRAMS - Uncomment one at a time for focused testing
# ==============================================================================

# Simple addition test
simple_add:
    addi x1, x0, 5
    addi x2, x0, 3
    add x3, x1, x2      # x3 = 8
    ecall

# Simple loop test
simple_loop:
    addi x1, x0, 0      # counter
    addi x2, x0, 5      # limit
loop:
    addi x1, x1, 1
    blt x1, x2, loop
    # x1 = 5 when done
    ecall

# Simple memory test
simple_memory:
    addi x1, x0, 42
    sw x1, 0(x0)
    lw x2, 0(x0)        # x2 should be 42
    ecall
