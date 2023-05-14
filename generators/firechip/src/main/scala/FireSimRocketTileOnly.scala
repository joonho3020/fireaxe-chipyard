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




class FireSimRocketTileOnly(implicit p: Parameters) extends RawModule {
  val buildtopClock = Wire(Clock())
  val buildtopReset = WireInit(false.B)

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

  val clockBridge = Module(new RationalClockBridge(Seq(RationalClock("FullRate", 1, 1), RationalClock("HalfRate", 1, 2))))
  buildtopClock := clockBridge.io.clocks(0)

  val core_bridge = Module(new TileCoreSideBridge())
  core_bridge.io.clock := buildtopClock
  core_bridge.io.reset := buildtopReset

  lazyModule match {
    case dut: TileOnlyChipTop =>
      println("Making TileOnlyChipTop Connections")
      dut.clockIO.head.clock := buildtopClock
      dut.clockIO.head.reset := buildtopReset

      dut.tileIO.interrupts       := core_bridge.io.in.interrupts
      dut.tileIO.hartid           := core_bridge.io.in.hartid
      dut.tileIO.tlmaster.a.ready := core_bridge.io.in.tlmaster_a_ready
      dut.tileIO.tlmaster.b.valid := core_bridge.io.in.tlmaster_b_valid
      dut.tileIO.tlmaster.b.bits  := core_bridge.io.in.tlmaster_b_bits
      dut.tileIO.tlmaster.c.ready := core_bridge.io.in.tlmaster_c_ready
      dut.tileIO.tlmaster.d.valid := core_bridge.io.in.tlmaster_d_valid
      dut.tileIO.tlmaster.d.bits  := core_bridge.io.in.tlmaster_d_bits
      dut.tileIO.tlmaster.e.ready := core_bridge.io.in.tlmaster_e_ready

      core_bridge.io.out.tlmaster_a_valid := dut.tileIO.tlmaster.a.valid
      core_bridge.io.out.tlmaster_a_bits  := dut.tileIO.tlmaster.a.bits
      core_bridge.io.out.tlmaster_b_ready := dut.tileIO.tlmaster.b.ready
      core_bridge.io.out.tlmaster_c_valid := dut.tileIO.tlmaster.c.valid
      core_bridge.io.out.tlmaster_c_bits  := dut.tileIO.tlmaster.c.bits
      core_bridge.io.out.tlmaster_d_ready := dut.tileIO.tlmaster.d.ready
      core_bridge.io.out.tlmaster_e_valid := dut.tileIO.tlmaster.e.valid
      core_bridge.io.out.tlmaster_e_bits  := dut.tileIO.tlmaster.e.bits
  }
}
