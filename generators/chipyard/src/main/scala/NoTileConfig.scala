package chipyard

import chisel3._
import chisel3.util._
import chisel3.experimental.{IO, DataMirror}
import org.chipsalliance.cde.config.{Parameters, Config, Field}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.tile._
import freechips.rocketchip.prci._
import freechips.rocketchip.subsystem.RocketCrossingParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import firesim.bridges._
import midas.widgets.{Bridge, PeekPokeBridge, RationalClockBridge, RationalClock, ResetPulseBridge, ResetPulseBridgeParameters}
import chipyard.{IsFireChip}


class ClockAndResetBundle extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
}

class PRCIClockResetBundle extends Bundle {
  val implicit_clock   = new ClockAndResetBundle
  val subsystem_cbus_0 = new ClockAndResetBundle
  val subsystem_cbus_0 = new ClockAndResetBundle
  val subsystem_mbus_0 = new ClockAndResetBundle
  val subsystem_fbus_0 = new ClockAndResetBundle
  val subsystem_pbus_0 = new ClockAndResetBundle
  val subsystem_sbus_1 = new ClockAndResetBundle
  val subsystem_sbus_0 = new ClockAndResetBundle
}


class AXIAWBundle extends Bundle {
  val id    = UInt(4.W)
  val addr  = UInt(35.W)
  val len   = UInt(8.W)
  val size  = UInt(3.W)
  val burst = UInt(2.W)
  val lock  = Bool()
  val cache = UInt(4.W)
  val prot  = UInt(3.W)
  val qos   = UInt(4.W)
}

class AXIWBundle extends Bundle {
  val data = UInt(64.W)
  val strb = UInt(8.W)
  val last = Bool()
}

class AXIBBundle extends Bundle {
  val id   = UInt(4.W)
  val resp = UInt(2.W)
}

class AXIARBundle extends Bundle {
  val id    = UInt(4.W)
  val addr  = UInt(35.W)
  val len   = UInt(8.W)
  val size  = UInt(3.W)
  val burst = UInt(2.W)
  val lock  = UInt(1.W)
  val cache = UInt(4.W)
  val prot  = UInt(3.W)
  val qos   = UInt(4.W)
}

class AXIRBundle extends Bundle {
  val id = UInt(4.W)
  val data = UInt(64.W)
  val resp = UInt(2.W)
  val last = Bool()
}

class AXIBundle extends Bundle {
  val aw = Decoupled(new AXIAWBundle)
  val w  = Decoupled(new AXIWBundle)
  val b  = Flipped(Decoupled(new AXIBBundle))
  val ar = Decoupled(new AXIARBundle)
  val r  = Flipped(Decoupled(new AXIRBundle))
}

class BlockDevReq extends Bundle {
  val write = Bool()
  val offset = UInt(32.W)
  val len = UInt(32.W)
}

class BlockDevData extends Bundle {
  val data = UInt(64.W)
}

class BlockDevResp extends Bundle {
  val data = UInt(64.W)
  val tag  = Bool()
}

class BlockDevInfo extends Bundle {
  val nsectors = UInt(32.W)
  val max_req_len = UInt(32.W)
}

class BlockDevBundle extends Bundle {
  val req  = Decoupled(new BlockDevReq)
  val data = Decoupled(new BlockDevData)
  val resp = Flipped(Decoupled(new BlockDevResp))
  val info = Input(new BlockDevInfo)
}

class BlockDevClockBundle extends Bundle {
  val clock = Output(Clock())
  val bits  = new BlockDevBundle
}

class SerialTLBundle extends Bundle {
  val in = Flipped(Decoupled(UInt(4.W)))
  val out = Decoupled(UInt(4.W))
}

class SerialTLClockBundle extends Bundle {
  val clock = Output(Clock())
  val bits  = new SerialTLBundle
}

class TraceInstBundle extends Bundle {
  val valid = Bool()
  val iaddr = UInt(40.W)
  val insn  = UInt(32.W)
  val priv  = UInt(3.W)
  val exception = Bool()
  val interrupt = Bool()
  val cause = UInt(64.W)
  val tval = UInt(40.W)
}

class TraceInstClockResetBundle extends Bundle {
  val clock = Clock()
  val reset = Bool()
  val insns_0 = new TraceInstBundle
}

class UARTBundle extends Bundle {
  val txd = Output(Bool())
  val rxd = Input(Bool())
}

class TileBoundaryIntBundle extends Bundle {
  val in_3_0 = Bool()
  val in_2_0 = Bool()
  val in_1_0 = Bool()
  val in_1_1 = Bool()
}

class TLAChannel extends Bundle {
  val opcode  = UInt(3.W )
  val param   = UInt(3.W )
  val size    = UInt(4.W )
  val source  = UInt(2.W )
  val address = UInt(35.W)
  val mask    = UInt(8.W )
  val data    = UInt(64.W)
}

class TLBChannel extends Bundle {
  val opcode  = UInt(3.W )
  val param   = UInt(2.W )
  val size    = UInt(4.W )
  val source  = UInt(2.W )
  val address = UInt(35.W)
  val mask    = UInt(8.W )
  val data    = UInt(64.W)
  val corrupt = Bool()
}

class TLCChannel extends Bundle {
  val opcode  = UInt(3.W )
  val param   = UInt(3.W )
  val size    = UInt(4.W )
  val source  = UInt(2.W )
  val address = UInt(35.W)
  val data    = UInt(64.W)
}

class TLDChannel extends Bundle {
  val opcode  = UInt(3.W )
  val param   = UInt(2.W )
  val size    = UInt(4.W )
  val source  = UInt(2.W )
  val sink    = UInt(3.W )
  val denied  = Bool()
  val data    = UInt(64.W)
  val corrupt = Bool()
}

class TLEChannel extends Bundle {
  val sink = UInt(3.W)
}

class TLChannelBundle extends Bundle {
  val a = Decoupled(new TLAChannel)
  val b = Flipped(Decoupled(new TLBChannel))
  val c = Decoupled(new TLCChannel)
  val d = Flipped(Decoupled(new TLDChannel))
  val e = Decoupled(new TLEChannel)
}

class TileBoundaryBridgeBundle extends Bundle {
  val broadcast_out_1_0 = Input(new TraceInstBundle)
  val int_local = Output(new TileBoundaryIntBundle)
  val hartid_in = Output(Bool())
  val tl_other_masters_out = Flipped(new TLChannelBundle)
}

class DigitalTopClockResetIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
  val auto_implicitClockGrouper_out = Flipped(new ClockAndResetBundle)
  val auto_prci_ctrl_domain_tileResetSetter_clock_in_member_allClocks = new PRCIClockResetBundle
  val auto_subsystem_mbus_fixedClockNode_out = Flipped(new ClockAndResetBundle)
  val mem_axi4_0 = new AXIBundle
  val custom_boot = Input(Bool())
  val bdev = new BlockDevClockBundle
  val serial_tl = new SerialTLClockBundle
  val traceIO_traces_0 = Output(new TraceInstClockResetBundle)
  val uart_0 = new UARTBundle
  val bridge = new TileBoundaryBridgeBundle
}

class DigitalTop_BLACKBOX extends BlackBox with HasBlackBoxResource {
  val io = IO(new DigitalTopClockResetIO)
  addResource("/vsrc/RocketUncore.sv")
}


class DigitalTopIO extends Bundle {
  val implicit_clock_reset = Flipped(new ClockAndResetBundle)
  val prci_ctrl = new PRCIClockResetBundle
  val mbus_fixedClockNode = Flipped(new ClockAndResetBundle)
  val axi4        = new AXIBundle
  val custom_boot = Input(Bool())
  val bdev        = new BlockDevClockBundle
  val serial_tl   = new SerialTLClockBundle
  val trace       = Output(new TraceInstClockResetBundle)
  val uart        = new UARTBundle
  val bridge      = new TileBoundaryBridgeBundle
}

class DigitalTopWrapper(implicit p: Parameters) extends LazyModule {
  override lazy val module = Module(new DigitalTopWrapperImp(this))
}

class DigitalTopWrapperImp(outer: DigitalTopWrapper)(implicit p: Parameters) extends LazyModuleImp(outer) {
  val io = IO(new DigitalTopIO)

  val dt = Module(new DigitalTop_BLACKBOX())

  val latencyToEndure = p(LatencyBetweenPartitions)
  val tileAChannelSkidBuffer = Module(new SkidBuffer(data = new TLAChannel, latencyToEndure=latencyToEndure))
  val tileCChannelSkidBuffer = Module(new SkidBuffer(data = new TLCChannel, latencyToEndure=latencyToEndure))
  val tileEChannelSkidBuffer = Module(new SkidBuffer(data = new TLEChannel, latencyToEndure=latencyToEndure))

  tileAChannelSkidBuffer.io.enq.valid := io.bridge.tl_other_masters_out.a.valid
  tileAChannelSkidBuffer.io.enq.bits  := io.bridge.tl_other_masters_out.a.bits
  io.bridge.tl_other_masters_out.a.ready := tileAChannelSkidBuffer.io.readyPropagate

  tileCChannelSkidBuffer.io.enq.valid := io.bridge.tl_other_masters_out.c.valid
  tileCChannelSkidBuffer.io.enq.bits  := io.bridge.tl_other_masters_out.c.bits
  io.bridge.tl_other_masters_out.c.ready := tileCChannelSkidBuffer.io.readyPropagate

  tileEChannelSkidBuffer.io.enq.valid := io.bridge.tl_other_masters_out.e.valid
  tileEChannelSkidBuffer.io.enq.bits  := io.bridge.tl_other_masters_out.e.bits
  io.bridge.tl_other_masters_out.e.ready := tileEChannelSkidBuffer.io.readyPropagate

  dt.io.clock := clock
  dt.io.reset := reset
  dt.io.auto_implicitClockGrouper_out := io.implicit_clock_reset
  dt.io.auto_prci_ctrl_domain_tileResetSetter_clock_in_member_allClocks := io.prci_ctrl
  dt.io.auto_subsystem_mbus_fixedClockNode_out := io.mbus_fixedClockNode
  dt.io.mem_axi4_0 <> io.axi4
  dt.io.custom_boot := io.custom_boot
  dt.io.bdev <> io.bdev
  dt.io.serial_tl <> io.serial_tl
  dt.io.traceIO_traces_0 <> io.trace
  dt.io.uart_0 <> io.uart
  dt.io.bridge.broadcast_out_1_0 <> io.bridge.broadcast_out_1_0
  io.io.bridge.int_local := dt.io.bridge.int_local
  io.io.bridge.hartid_in := dt.io.bridge.hartid_in
  dt.io.bridge.tl_other_masters_out.a <> tileAChannelSkidBuffer.io.deq
  io.bridge.tl_other_masters_out.b    <> dt.io.bridge.tl_other_masters_out.b
  dt.io.bridge.tl_other_masters_out.c <> tileCChannelSkidBuffer.io.deq
  io.bridge.tl_other_masters_out.d    <> dt.io.bridge.tl_other_masters_out.d
  dt.io.bridge.tl_other_masters_out.e <> tileEChannelSkidBuffer.io.deq
}


class NoTileChipTop(implicit p: Parameters) extends LazyModule {
  lazy val lazyTop = LazyModule(p(BuildSystem(p))).suggestName("ChipTop")
  lazy val module: LazyModuleImpLike = new LazyRawModuleImp(this) { }

  val implicitClockSourceNode = ClockSourceNode(Seq(ClockSourceParameters(name = Some("top_clock"))))
  val implicitClockSinkNode   = ClockSinkNode(Seq(ClockSinkParameters(name = Some("implicit_clock"))))

  implicitClockSinkNode := implicitClockSourceNode

  val topIO = InModuleBody {
    lazyTop.asInstanceOf[DigitalTopWrapper].module match { case l: LazyModuleImp => {
      val implicit_clock = implicitClockSinkNode.in.head._1.clock
      val implicit_reset = implicitClockSinkNode.in.head._1.reset
      l.clock := implicit_clock
      l.reset := implicit_reset

      val tio = IO(DataMirror.internal.chiselTypeClone[DigitalTopIO](l.io))
      l.io <> tio
      tio
    }}
  }

  val clockIO = InModuleBody {
    implicitClockSourceNode.makeIOs()
  }
}

class WithNoTileConfig extends Config((site, here, up) => {
  case BuildSystem => (p: Parameters) => new DigitalTopWrapper()(p)
  case BuildTop    => (p: Parameters) => new NoTileChipTop()(p)
})

class FireSimNoTileConfig extends Config(
  new chipyard.WithPartitionLatency(32) ++
  new chipyard.WithNoTileConfig
  )
