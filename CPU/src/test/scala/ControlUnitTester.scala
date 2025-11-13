import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ControlUnitTester extends AnyFlatSpec with ChiselScalatestTester {

  "ControlUnit" should "decode opcodes and set control signals correctly" in {
    test(new ControlUnit) { c =>
      def makeInstr(
          op: Int,
          reg1: Int,
          reg2: Int,
          reg3: Int,
          imm: Int
      ): BigInt = {
        ((BigInt(imm & 0xffff) << 16) |
          (BigInt(reg3 & 0xf) << 12) |
          (BigInt(reg2 & 0xf) << 8) |
          (BigInt(reg1 & 0xf) << 4) |
          BigInt(op & 0xf))
      }

      def checkOutputs(
          instr: BigInt,
          expectedOp: Int,
          expectedSignals: (
              Boolean,
              Boolean,
              Boolean,
              Boolean,
              Boolean,
              Boolean,
              Boolean,
              Boolean
          ),
          reg1: Int,
          reg2: Int,
          reg3: Int,
          imm: Int
      ): Unit = {
        c.io.instruction.poke(instr.U)
        c.clock.step(1)

        c.io.opcode.expect(expectedOp.U)
        c.io.reg1.expect(reg1.U)
        c.io.reg2.expect(reg2.U)
        c.io.reg3.expect(reg3.U)
        c.io.imm.expect(imm.U)

        val (li, lb, sb, branch, add, addi, j, exit) = expectedSignals
        c.io.li.expect(li.B)
        c.io.lb.expect(lb.B)
        c.io.sb.expect(sb.B)
        c.io.branch.expect(branch.B)
        c.io.add.expect(add.B)
        c.io.addi.expect(addi.B)
        c.io.j.expect(j.B)
        c.io.exit.expect(exit.B)
      }

      val reg1 = 1
      val reg2 = 2
      val reg3 = 3
      val imm = 0xabcd

      val tests = Seq(
        (0x1, (false, false, false, false, true, false, false, false)),
        (0x2, (false, false, false, false, false, true, false, false)),
        (0x3, (true, false, false, false, false, false, false, false)),
        (0x4, (false, true, false, false, false, false, false, false)),
        (0x5, (false, false, true, false, false, false, false, false)),
        (0x6, (false, false, false, true, false, false, false, false)),
        (0x7, (false, false, false, false, false, false, true, false)),
        (0x8, (false, false, false, false, false, false, false, true))
      )

      for ((opcode, signals) <- tests) {
        val instr = makeInstr(opcode, reg1, reg2, reg3, imm)
        checkOutputs(instr, opcode, signals, reg1, reg2, reg3, imm)
      }
    }
  }
}
