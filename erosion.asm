.data
    in_image: .word 0x400     # 20x20 words (each pixel = 1 word)
    out_image: .word 1600

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
    add t2, t2, t1     # t2 = x*20 + y
    slli t2, t2, 2     # *4 (word offset)
    la t3, in_image
    add t3, t3, t2
    lw t4, 0(t3)       # t4 = in_image[x][y]

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
    add t2, t2, t1
    slli t2, t2, 2
    la t3, in_image
    add t3, t3, t2
    lw t5, 0(t3)
    beqz t5, darken

    # in_image[x+1][y]
    addi s2, t0, 1
    mul t2, s2, s0
    add t2, t2, t1
    slli t2, t2, 2
    la t3, in_image
    add t3, t3, t2
    lw t5, 0(t3)
    beqz t5, darken

    # in_image[x][y-1]
    mul t2, t0, s0
    addi s2, t1, -1
    add t2, t2, s2
    slli t2, t2, 2
    la t3, in_image
    add t3, t3, t2
    lw t5, 0(t3)
    beqz t5, darken

    # in_image[x][y+1]
    mul t2, t0, s0
    addi s2, t1, 1
    add t2, t2, s2
    slli t2, t2, 2
    la t3, in_image
    add t3, t3, t2
    lw t5, 0(t3)
    beqz t5, darken

    # None were black → do not erode
    la t3, out_image
    mul t2, t0, s0
    add t2, t2, t1
    slli t2, t2, 2
    add t3, t3, t2
    li t5, 255
    sw t5, 0(t3)
    j after_pixel

darken:
    # out_image[x][y] = 0
    la t3, out_image
    mul t2, t0, s0
    add t2, t2, t1
    slli t2, t2, 2
    add t3, t3, t2
    sw x0, 0(t3)

skip_white_check:

after_pixel:
    addi t1, t1, 1
    blt t1, s0, inner_loop

    addi t0, t0, 1
    blt t0, s0, outer_loop

done:
    li a0, 10
    ecall
