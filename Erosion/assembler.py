#!/usr/bin/env python3
"""
Simple RISC-V Assembler for Chisel CPU
Supports: li, lw, sw, add, addi, mul, beq, bne, blt, beqz, j, ecall
"""

# Register mapping
REGISTERS = {
    'x0': 0, 'zero': 0,
    'x1': 1, 'ra': 1,
    'x2': 2, 'sp': 2,
    'x3': 3, 'gp': 3,
    'x4': 4, 'tp': 4,
    'x5': 5, 't0': 5,
    'x6': 6, 't1': 6,
    'x7': 7, 't2': 7,
    'x8': 8, 's0': 8, 'fp': 8,
    'x9': 9, 's1': 9,
    'x10': 10, 'a0': 10,
    'x11': 11, 'a1': 11,
    'x12': 12, 'a2': 12,
    'x13': 13, 'a3': 13,
    'x14': 14, 'a4': 14,
    'x15': 15, 'a5': 15,
    'x16': 16, 'a6': 16,
    'x17': 17, 'a7': 17,
    'x18': 18, 's2': 18,
    'x19': 19, 's3': 19,
    'x20': 20, 's4': 20,
    'x21': 21, 's5': 21,
    'x22': 22, 's6': 22,
    'x23': 23, 's7': 23,
    'x24': 24, 's8': 24,
    'x25': 25, 's9': 25,
    'x26': 26, 's10': 26,
    'x27': 27, 's11': 27,
    'x28': 28, 't3': 28,
    'x29': 29, 't4': 29,
    'x30': 30, 't5': 30,
    'x31': 31, 't6': 31,
}

def sign_extend(value, bits):
    """Sign extend a value to 32 bits"""
    sign_bit = 1 << (bits - 1)
    return (value & (sign_bit - 1)) - (value & sign_bit)

def encode_r_type(opcode, rd, funct3, rs1, rs2, funct7):
    """Encode R-type instruction"""
    return (funct7 << 25) | (rs2 << 20) | (rs1 << 15) | (funct3 << 12) | (rd << 7) | opcode

def encode_i_type(opcode, rd, funct3, rs1, imm):
    """Encode I-type instruction"""
    imm = imm & 0xFFF  # 12-bit immediate
    return (imm << 20) | (rs1 << 15) | (funct3 << 12) | (rd << 7) | opcode

def encode_s_type(opcode, funct3, rs1, rs2, imm):
    """Encode S-type instruction"""
    imm = imm & 0xFFF
    imm_11_5 = (imm >> 5) & 0x7F
    imm_4_0 = imm & 0x1F
    return (imm_11_5 << 25) | (rs2 << 20) | (rs1 << 15) | (funct3 << 12) | (imm_4_0 << 7) | opcode

def encode_b_type(opcode, funct3, rs1, rs2, imm):
    """Encode B-type instruction"""
    imm = imm & 0x1FFF  # 13-bit immediate
    imm_12 = (imm >> 12) & 0x1
    imm_10_5 = (imm >> 5) & 0x3F
    imm_4_1 = (imm >> 1) & 0xF
    imm_11 = (imm >> 11) & 0x1
    return (imm_12 << 31) | (imm_10_5 << 25) | (rs2 << 20) | (rs1 << 15) | (funct3 << 12) | (imm_4_1 << 8) | (imm_11 << 7) | opcode

def encode_u_type(opcode, rd, imm):
    """Encode U-type instruction"""
    imm = (imm & 0xFFFFF) << 12  # 20-bit immediate, shifted
    return imm | (rd << 7) | opcode

def encode_j_type(opcode, rd, imm):
    """Encode J-type instruction"""
    imm = imm & 0x1FFFFF  # 21-bit immediate
    imm_20 = (imm >> 20) & 0x1
    imm_10_1 = (imm >> 1) & 0x3FF
    imm_11 = (imm >> 11) & 0x1
    imm_19_12 = (imm >> 12) & 0xFF
    return (imm_20 << 31) | (imm_19_12 << 12) | (imm_11 << 20) | (imm_10_1 << 21) | (rd << 7) | opcode

def parse_register(reg_str):
    """Parse register name to number"""
    reg_str = reg_str.strip().lower()
    if reg_str not in REGISTERS:
        raise ValueError(f"Unknown register: {reg_str}")
    return REGISTERS[reg_str]

def parse_immediate(imm_str):
    """Parse immediate value"""
    imm_str = imm_str.strip()
    if imm_str.startswith('0x'):
        return int(imm_str, 16)
    return int(imm_str)

def parse_memory_operand(operand):
    """Parse memory operand like '0(t2)' -> (offset, register)"""
    operand = operand.strip()
    if '(' in operand:
        offset, reg = operand.split('(')
        reg = reg.rstrip(')')
        offset = parse_immediate(offset) if offset else 0
        return offset, parse_register(reg)
    raise ValueError(f"Invalid memory operand: {operand}")

def assemble(assembly_code):
    """Assemble RISC-V code to binary"""
    lines = assembly_code.strip().split('\n')
    labels = {}
    instructions = []
    address = 0
    
    # First pass: collect labels
    for line in lines:
        line = line.split('#')[0].strip()  # Remove comments
        if not line or line.startswith('.'):
            continue
        if line.endswith(':'):
            labels[line[:-1]] = address
        else:
            instructions.append((address, line))
            address += 4
    
    # Second pass: assemble instructions
    binary = []
    for addr, line in instructions:
        parts = line.split()
        if not parts:
            continue
        
        instr = parts[0].lower()
        
        if instr == 'li':  # Pseudo-instruction: li rd, imm
            rd = parse_register(parts[1].rstrip(','))
            imm = parse_immediate(parts[2])
            # Use addi rd, x0, imm
            binary.append(encode_i_type(0b0010011, rd, 0b000, 0, imm))
        
        elif instr == 'lw':
            rd = parse_register(parts[1].rstrip(','))
            offset, rs1 = parse_memory_operand(parts[2])
            binary.append(encode_i_type(0b0000011, rd, 0b010, rs1, offset))
        
        elif instr == 'sw':
            rs2 = parse_register(parts[1].rstrip(','))
            offset, rs1 = parse_memory_operand(parts[2])
            binary.append(encode_s_type(0b0100011, 0b010, rs1, rs2, offset))
        
        elif instr == 'add':
            rd = parse_register(parts[1].rstrip(','))
            rs1 = parse_register(parts[2].rstrip(','))
            rs2 = parse_register(parts[3])
            binary.append(encode_r_type(0b0110011, rd, 0b000, rs1, rs2, 0b0000000))
        
        elif instr == 'addi':
            rd = parse_register(parts[1].rstrip(','))
            rs1 = parse_register(parts[2].rstrip(','))
            imm = parse_immediate(parts[3])
            binary.append(encode_i_type(0b0010011, rd, 0b000, rs1, imm))
        
        elif instr == 'mul':
            rd = parse_register(parts[1].rstrip(','))
            rs1 = parse_register(parts[2].rstrip(','))
            rs2 = parse_register(parts[3])
            binary.append(encode_r_type(0b0110011, rd, 0b000, rs1, rs2, 0b0000001))
        
        elif instr == 'beq':
            rs1 = parse_register(parts[1].rstrip(','))
            rs2 = parse_register(parts[2].rstrip(','))
            label = parts[3]
            offset = labels[label] - addr
            binary.append(encode_b_type(0b1100011, 0b000, rs1, rs2, offset))
        
        elif instr == 'bne':
            rs1 = parse_register(parts[1].rstrip(','))
            rs2 = parse_register(parts[2].rstrip(','))
            label = parts[3]
            offset = labels[label] - addr
            binary.append(encode_b_type(0b1100011, 0b001, rs1, rs2, offset))
        
        elif instr == 'blt':
            rs1 = parse_register(parts[1].rstrip(','))
            rs2 = parse_register(parts[2].rstrip(','))
            label = parts[3]
            offset = labels[label] - addr
            binary.append(encode_b_type(0b1100011, 0b100, rs1, rs2, offset))
        
        elif instr == 'beqz':  # Pseudo-instruction: beqz rs1, label -> beq rs1, x0, label
            rs1 = parse_register(parts[1].rstrip(','))
            label = parts[2]
            offset = labels[label] - addr
            binary.append(encode_b_type(0b1100011, 0b000, rs1, 0, offset))
        
        elif instr == 'j':  # Pseudo-instruction: j label -> jal x0, label
            label = parts[1]
            offset = labels[label] - addr
            binary.append(encode_j_type(0b1101111, 0, offset))
        
        elif instr == 'ecall':
            binary.append(0b00000000000000000000000001110011)
        
        else:
            raise ValueError(f"Unsupported instruction: {instr}")
    
    return binary

# Example usage
assembly_code = """
.text
main:
    li t0, 0           # x = 0
    li s0, 20          # width/height = 20
    li s1, 19          # last index = 19

outer_loop:
    li t1, 0           # y = 0

inner_loop:
    # Compute address in_image[x][y]
    mul t2, t0, s0     # t2 = x * 20
    add t2, t2, t1     # t2 = x*20 + y (address 0-399)
    lw t4, 0(t2)       # t4 = in_image[x][y]

    # Border check
    beq t0, x0, darken
    beq t1, x0, darken
    beq t0, s1, darken
    beq t1, s1, darken

    # Check only white pixels (255)
    li t5, 255
    bne t4, t5, skip_white_check

    # in_image[x-1][y]
    addi s2, t0, -1
    mul t2, s2, s0
    add t2, t2, t1     # address = (x-1)*20 + y
    lw t5, 0(t2)
    beqz t5, darken

    # in_image[x+1][y]
    addi s2, t0, 1
    mul t2, s2, s0
    add t2, t2, t1     # address = (x+1)*20 + y
    lw t5, 0(t2)
    beqz t5, darken

    # in_image[x][y-1]
    mul t2, t0, s0
    addi s2, t1, -1
    add t2, t2, s2     # address = x*20 + (y-1)
    lw t5, 0(t2)
    beqz t5, darken

    # in_image[x][y+1]
    mul t2, t0, s0
    addi s2, t1, 1
    add t2, t2, s2     # address = x*20 + (y+1)
    lw t5, 0(t2)
    beqz t5, darken

    # None were black → do not erode
    mul t2, t0, s0
    add t2, t2, t1
    addi t2, t2, 400   # output address = (x*20 + y) + 400
    li t5, 255
    sw t5, 0(t2)
    j after_pixel

darken:
    # out_image[x][y] = 0
    mul t2, t0, s0
    add t2, t2, t1
    addi t2, t2, 400   # output address = (x*20 + y) + 400
    sw x0, 0(t2)

skip_white_check:

after_pixel:
    addi t1, t1, 1
    blt t1, s0, inner_loop

    addi t0, t0, 1
    blt t0, s0, outer_loop

done:
    li a0, 10
    ecall
"""

if __name__ == "__main__":
    binary_code = assemble(assembly_code)
    
    print("Chisel binary string format:")
    print('val program1 = "')
    for instr in binary_code:
        print(f'  {instr:032b}' + '"' + (' +' if instr != binary_code[-1] else ''))
    print()
    
    print(f"\nTotal instructions: {len(binary_code)}")
    
    # Also print hex for reference
    print("\nHex reference:")
    for i, instr in enumerate(binary_code):
        print(f"{i*4:04x}: {instr:08x}")
    
    # Save as binary string file for easy copy-paste
    with open("program_chisel.txt", "w") as f:
        f.write('val program1 = "\n')
        for i, instr in enumerate(binary_code):
            f.write(f'  {instr:032b}"')
            if i < len(binary_code) - 1:
                f.write(' +\n')
            else:
                f.write('\n')
    
    print("\nChisel-formatted binary saved to 'program_chisel.txt'")
