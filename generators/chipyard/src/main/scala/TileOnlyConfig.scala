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


class TracerVBundle extends Bundle {
  val valid     = Bool()
  val iaddr     = UInt(39.W)
  val insn      = UInt(32.W)
  val priv      = UInt(3.W)
  val exception = Bool()
  val interrupt = Bool()
  val cause     = UInt(64.W)
  val tval      = UInt(40.W)
}

class InterruptBundle extends Bundle {
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


class RocketTileBlackBoxIO extends Bundle {
  val clock                     = Input(Clock())
  val reset                     = Input(Reset())
  val auto_broadcast_out_1_0    = Output(new TracerVBundle)
  val auto_int_local            = Input(new InterruptBundle)
  val auto_hartid_in            = Input(UInt(1.W))
  val auto_tl_other_masters_out = new TLChannelBundle
}

class RocketTile_BLACKBOX extends BlackBox with HasBlackBoxResource {
  val io = IO(new RocketTileBlackBoxIO)
  addResource("/vsrc/RocketTileBlackBox.sv")
}

class RocketTilePRCIDomainIO extends Bundle {
// TODO : optionally enable tracing
// val trace      = Output(new TracerVBundle)
  val interrupts = Input (new InterruptBundle)
  val hartid     = Input (UInt(1.W))
  val tlmaster   = new TLChannelBundle
}

class TilePRCIDomain(implicit p: Parameters) extends LazyModule {
  override lazy val module = Module(new TilePRCIDomainImp(this))
}

class TilePRCIDomainImp(outer: TilePRCIDomain)(implicit p: Parameters) extends LazyModuleImp(outer) {
  val io = IO(new RocketTilePRCIDomainIO)

  dontTouch(io)

  val latencyToEndure = p(LatencyBetweenPartitions)
  val tileBChannelSkidBuffer = Module(new SkidBuffer(data=new TLBChannel, latencyToEndure=latencyToEndure))
  val tileDChannelSkidBuffer = Module(new SkidBuffer(data=new TLDChannel, latencyToEndure=latencyToEndure))

  val tile = Module(new RocketTile_BLACKBOX())
  tile.io.clock := clock
  tile.io.reset := reset.asBool
  tile.io.auto_int_local := io.interrupts
  tile.io.auto_hartid_in := io.hartid

  io.tlmaster.a <> tile.io.auto_tl_other_masters_out.a
  io.tlmaster.c <> tile.io.auto_tl_other_masters_out.c
  io.tlmaster.e <> tile.io.auto_tl_other_masters_out.e

  tile.io.auto_tl_other_masters_out.b <> tileBChannelSkidBuffer.io.deq
  tile.io.auto_tl_other_masters_out.d <> tileDChannelSkidBuffer.io.deq

  tileBChannelSkidBuffer.io.enq.valid := io.tlmaster.b.valid
  tileBChannelSkidBuffer.io.enq.bits  := io.tlmaster.b.bits
  io.tlmaster.b.ready := tileBChannelSkidBuffer.io.readyPropagate

  tileDChannelSkidBuffer.io.enq.valid := io.tlmaster.d.valid
  tileDChannelSkidBuffer.io.enq.bits  := io.tlmaster.d.bits
  io.tlmaster.d.ready := tileDChannelSkidBuffer.io.readyPropagate

  assert(tileBChannelSkidBuffer.io.enq.ready === true.B, "tileBChannelSkidBuffer full")
  assert(tileDChannelSkidBuffer.io.enq.ready === true.B, "tileDChannelSkidBuffer full")
}

class TileOnlyChipTop(implicit p: Parameters) extends LazyModule {
  lazy val lazyTile = LazyModule(p(BuildSystem)(p)).suggestName("TilePRCIDomain")
  lazy val module: LazyModuleImpLike = new LazyRawModuleImp(this) { }

  val implicitClockSourceNode = ClockSourceNode(Seq(ClockSourceParameters(name = Some("top_clock"))))
  val implicitClockSinkNode = ClockSinkNode(Seq(ClockSinkParameters(name = Some("implicit_clock"))))

  implicitClockSinkNode := implicitClockSourceNode

  val tileIO = InModuleBody {
    lazyTile.asInstanceOf[TilePRCIDomain].module match { case l: LazyModuleImp => {
      val implicit_clock = implicitClockSinkNode.in.head._1.clock
      val implicit_reset = implicitClockSinkNode.in.head._1.reset
      l.clock := implicit_clock
      l.reset := implicit_reset

      val tio = IO(DataMirror.internal.chiselTypeClone[RocketTilePRCIDomainIO](l.io))
      l.io <> tio
      tio
    }}
  }

  val clockIO = InModuleBody {
    implicitClockSourceNode.makeIOs()
  }
}

class WithTileOnlyConfig extends Config((site, here, up) => {
  case BuildSystem => (p: Parameters) => new TilePRCIDomain()(p)
  case BuildTop => (p: Parameters) => new TileOnlyChipTop()(p)
})

// This is more of a placeholder right now
class WithTileOnlyFireSimBridges extends Config((site, here, up) => {
  case IsFireChip => true
})

class FireSimRocketTileConfig extends Config(
  new chipyard.WithPartitionLatency(32) ++
  new chipyard.WithTileOnlyConfig ++
  new chipyard.WithTileOnlyFireSimBridges
)
