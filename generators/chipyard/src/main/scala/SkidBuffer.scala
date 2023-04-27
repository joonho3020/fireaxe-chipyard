package chipyard

import chisel3._
import chisel3.util._


class SkidBuffer[T <: Data](data: T, latencyToEndure: Int) extends Module {
  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(data))
    val readyPropagate = Output(Bool())
    val deq = Decoupled(data)
  })

  val depth = 2 * latencyToEndure
  val buf = Module(new Queue(data, depth, flow=true))

  io.deq <> buf.io.deq
  buf.io.enq <> io.enq

  io.readyPropagate := (buf.io.count === 0.U)
}
