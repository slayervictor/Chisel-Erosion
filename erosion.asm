
.data
foo:
	.asciiz "Bar\n"
.text
    li x1, 5 # loads 5 into x1
    li x2, 7 # loads 7 into x2
    add x1, x1, x2 # adds x1 and x2 and puts the result into x1
    li x7, 12
    beq x1,x7, printBar
    j endProgram

    
printBar: 
    la a1, foo
    li a0, 4
    ecall

endProgram:
	li a0, 10
    ecall 
