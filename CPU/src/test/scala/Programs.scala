import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import java.util

object Programs {
  val program1 = Array(
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W),
    "h00000000".U(32.W)
  )

  val erosion = Array(
    "hFFFF0003".U(32.W), // li a0, -1
    "h00000023".U(32.W), // li a2, 0
    "h00000013".U(32.W), // li a1, 0
    "h00010002".U(32.W), // addi a0, a0, 1
    "hFFEC0042".U(32.W), // addi t0, a0, -20
    "h001F0046".U(32.W), // beqz t0, end (inst. 31)
    "h00000244".U(32.W), // lb t0, 0(a2)
    "h00000432".U(32.W), // addi a3, t0, 0
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)
    // check rows
    "h001D0006".U(32.W), // beqz a0, set_zero (inst. 29)
    "hFFED0042".U(32.W), // addi t0, a0, -19
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)
    // check columns
    "h001D0016".U(32.W), // beqz a1, set_zero (inst. 29)
    "hFFED0142".U(32.W), // addi t0, a1, -19
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)
    // right neighbor
    "h00010244".U(32.W), // lb t0, 1(a2)
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)
    // left neighbor
    "hFFFF0244".U(32.W), // lb t0, -1(a2)
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)
    // above neighbor
    "hFFEC0244".U(32.W), // lb t0, -20(a2)
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)
    // below neighbor
    "h00140244".U(32.W), // lb t0, 20(a2)
    "h001D0046".U(32.W), // beqz t0, set_zero (inst. 29)

    "h01900235".U(32.W), // sb a3, 400(a2)
    "h00010222".U(32.W), // addi a2, a2, 1
    "h00010112".U(32.W), // addi a1, a1, 1
    "hFFEC0142".U(32.W), // addi t0, a1, -20
    "h00020046".U(32.W), // beqz t0, outer_loop (inst. 2)
    "h00060007".U(32.W), // j inner_loop (inst. 6)
    "h00000033".U(32.W), // li a3, 0
    "h00170007".U(32.W), // j next (inst. 23)
    "h00000008".U(32.W) // end
  )
}
