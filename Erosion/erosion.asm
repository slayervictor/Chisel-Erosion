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
