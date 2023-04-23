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

      dut.tileIO.auto_int_local_in_3_0                    := core_bridge.io.in.debug
      dut.tileIO.auto_int_local_in_2_0                    := core_bridge.io.in.mtip
      dut.tileIO.auto_int_local_in_1_0                    := core_bridge.io.in.msip
      dut.tileIO.auto_int_local_in_1_1                    := core_bridge.io.in.meip
      dut.tileIO.auto_int_local_in_0_0                    := core_bridge.io.in.seip
      dut.tileIO.auto_hartid_in                           := core_bridge.io.in.hartid
      dut.tileIO.auto_tl_other_masters_out_a_ready        := core_bridge.io.in.tlmaster_a_ready
      dut.tileIO.auto_tl_other_masters_out_b_valid        := core_bridge.io.in.tlmaster_b_valid
      dut.tileIO.auto_tl_other_masters_out_b_bits_opcode  := core_bridge.io.in.tlmaster_b_bits_opcode
      dut.tileIO.auto_tl_other_masters_out_b_bits_param   := core_bridge.io.in.tlmaster_b_bits_param
      dut.tileIO.auto_tl_other_masters_out_b_bits_size    := core_bridge.io.in.tlmaster_b_bits_size
      dut.tileIO.auto_tl_other_masters_out_b_bits_source  := core_bridge.io.in.tlmaster_b_bits_source
      dut.tileIO.auto_tl_other_masters_out_b_bits_address := core_bridge.io.in.tlmaster_b_bits_address
      dut.tileIO.auto_tl_other_masters_out_b_bits_mask    := core_bridge.io.in.tlmaster_b_bits_mask
      dut.tileIO.auto_tl_other_masters_out_b_bits_data    := core_bridge.io.in.tlmaster_b_bits_data
      dut.tileIO.auto_tl_other_masters_out_b_bits_corrupt := core_bridge.io.in.tlmaster_b_bits_corrupt
      dut.tileIO.auto_tl_other_masters_out_c_ready        := core_bridge.io.in.tlmaster_c_ready
      dut.tileIO.auto_tl_other_masters_out_d_valid        := core_bridge.io.in.tlmaster_d_valid
      dut.tileIO.auto_tl_other_masters_out_d_bits_opcode  := core_bridge.io.in.tlmaster_d_bits_opcode
      dut.tileIO.auto_tl_other_masters_out_d_bits_param   := core_bridge.io.in.tlmaster_d_bits_param
      dut.tileIO.auto_tl_other_masters_out_d_bits_size    := core_bridge.io.in.tlmaster_d_bits_size
      dut.tileIO.auto_tl_other_masters_out_d_bits_source  := core_bridge.io.in.tlmaster_d_bits_source
      dut.tileIO.auto_tl_other_masters_out_d_bits_sink    := core_bridge.io.in.tlmaster_d_bits_sink
      dut.tileIO.auto_tl_other_masters_out_d_bits_denied  := core_bridge.io.in.tlmaster_d_bits_denied
      dut.tileIO.auto_tl_other_masters_out_d_bits_data    := core_bridge.io.in.tlmaster_d_bits_data
      dut.tileIO.auto_tl_other_masters_out_d_bits_corrupt := core_bridge.io.in.tlmaster_d_bits_corrupt
      dut.tileIO.auto_tl_other_masters_out_e_ready        := core_bridge.io.in.tlmaster_e_ready

      core_bridge.io.out.wfi                     := dut.tileIO.auto_wfi_out_0
      core_bridge.io.out.tlmaster_a_valid        := dut.tileIO.auto_tl_other_masters_out_a_valid
      core_bridge.io.out.tlmaster_a_bits_opcode  := dut.tileIO.auto_tl_other_masters_out_a_bits_opcode
      core_bridge.io.out.tlmaster_a_bits_param   := dut.tileIO.auto_tl_other_masters_out_a_bits_param
      core_bridge.io.out.tlmaster_a_bits_size    := dut.tileIO.auto_tl_other_masters_out_a_bits_size
      core_bridge.io.out.tlmaster_a_bits_source  := dut.tileIO.auto_tl_other_masters_out_a_bits_source
      core_bridge.io.out.tlmaster_a_bits_address := dut.tileIO.auto_tl_other_masters_out_a_bits_address
      core_bridge.io.out.tlmaster_a_bits_mask    := dut.tileIO.auto_tl_other_masters_out_a_bits_mask
      core_bridge.io.out.tlmaster_a_bits_data    := dut.tileIO.auto_tl_other_masters_out_a_bits_data
      core_bridge.io.out.tlmaster_a_bits_corrupt := dut.tileIO.auto_tl_other_masters_out_a_bits_corrupt
      core_bridge.io.out.tlmaster_b_ready        := dut.tileIO.auto_tl_other_masters_out_b_ready
      core_bridge.io.out.tlmaster_c_valid        := dut.tileIO.auto_tl_other_masters_out_c_valid
      core_bridge.io.out.tlmaster_c_bits_opcode  := dut.tileIO.auto_tl_other_masters_out_c_bits_opcode
      core_bridge.io.out.tlmaster_c_bits_param   := dut.tileIO.auto_tl_other_masters_out_c_bits_param
      core_bridge.io.out.tlmaster_c_bits_size    := dut.tileIO.auto_tl_other_masters_out_c_bits_size
      core_bridge.io.out.tlmaster_c_bits_source  := dut.tileIO.auto_tl_other_masters_out_c_bits_source
      core_bridge.io.out.tlmaster_c_bits_address := dut.tileIO.auto_tl_other_masters_out_c_bits_address
      core_bridge.io.out.tlmaster_c_bits_data    := dut.tileIO.auto_tl_other_masters_out_c_bits_data
      core_bridge.io.out.tlmaster_c_bits_corrupt := dut.tileIO.auto_tl_other_masters_out_c_bits_corrupt
      core_bridge.io.out.tlmaster_d_ready        := dut.tileIO.auto_tl_other_masters_out_d_ready
      core_bridge.io.out.tlmaster_e_valid        := dut.tileIO.auto_tl_other_masters_out_e_valid
      core_bridge.io.out.tlmaster_e_bits_sink    := dut.tileIO.auto_tl_other_masters_out_e_bits_sink
  }
}
