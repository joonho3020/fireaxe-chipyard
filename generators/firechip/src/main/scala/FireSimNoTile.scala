package firesim.firesim

import chisel3._
import chisel3.util._
import chisel3.util._
import chisel3.experimental._
import org.chipsalliance.cde.config.{Parameters, Config, Field}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.tile._
import freechips.rocketchip.subsystem.RocketCrossingParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import firesim.bridges._
import midas.widgets.{Bridge, PeekPokeBridge, RationalClockBridge, RationalClock, ResetPulseBridge, ResetPulseBridgeParameters}
import chipyard._



// TODO :  Check generated verilog top level to check for ...
// - if connections are valid
// - compare it with the bridgebinder case
class FireSimNoTile(implicit p: Parameters) extends RawModule {
  val buildtopClock = Wire(Clock())
  val buildtopReset = Wire(Reset())

  val dummy = WireInit(false.B)
  val peekPokeBridge = PeekPokeBridge(buildtopClock, dummy)

  val resetBridge = Module(new ResetPulseBridge(ResetPulseBridgeParameters()))
  resetBridge.io.clock := buildtopClock
  buildtopReset := resetBridge.io.reset
  midas.targetutils.GlobalResetCondition(buildtopReset)

  def dutReset = { require(false, "dutReset should not be used in Firesim"); false.B }
  def success = { require(false, "success should not be used in Firesim"); false.B }

  val lazyModule = LazyModule(p(BuildTop)(p))
  val module = Module(lazyModule.module)

  val blockDevBridge = Module(new BlockDevBridge)
  blockDevBridge.io.reset := buildtopReset
  blockDevBridge.io.clock := module.io.bdev.clock
  blockDevBridge.io.bdev.req <> module.io.bdev.req
  blockDevBridge.io.bdev.data <> module.io.bdev.data
  module.io.bdev.resp <> blockDevBridge.io.bdev.resp
  module.io.bdev.info <> blockDevBridge.io.bdev.info


// val serialBits = SerialAdapter.asyncQueue(module.io.serial_tl.bits.asInstanceOf[SerialIO], buildTopClock, buildTopReset)
// val seralRam = withClockAndReset(buildtopClock, buildtopReset) {
// SerialAdapter.connectHarnessRam(
// }
// val serialBridge = Module(new


}
