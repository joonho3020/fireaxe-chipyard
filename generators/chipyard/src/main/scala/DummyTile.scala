package chipyard

import chisel3._
import chisel3.util._
import chisel3.Printable
import chisel3.experimental.{Analog, BaseModule, DataMirror, Direction}
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.util._
import freechips.rocketchip.prci._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.jtag.{JTAGIO}
import testchipip.{SerialTLKey, UARTAdapter, SimDRAM}
import chipyard.iobinders._
import chipyard.clocking._
import barstools.iocell.chisel._
import chipyard.harness.{BuildTop}
import firesim.bridges._


// List(TLEdgeParameters(
// TLMasterPortParameters(
// List(
// TLMasterParameters(Core 0 DCache, IdRange(0,1), List(), List(AddressSet(0x0, ~0x0)), Set(), false, false, P, TBALGFPH, false),
// TLMasterParameters(Core 0 DCache MMIO, IdRange(1,2), List(), List(AddressSet(0x0, ~0x0)), Set(), false, true, , TBALGFPH, false),
// TLMasterParameters(Core 0 ICache, IdRange(2,3), List(), List(AddressSet(0x0, ~0x0)), Set(), false, false, , TBALGFPH, false)
// ),
// TLChannelBeatBytes(None,None,None,None), 0, List(), List(), List()
// ),
// TLSlavePortParameters(
// List(
// TLSlaveParameters(error, List(AddressSet(0x3000, 0xfff)), List(Resource(freechips.rocketchip.diplomacy.SimpleDevice@1efb3184,reg)), VOLATILE, true, Some(0), ALGFPH, PALGFPH,        false, true, true),
// TLSlaveParameters(l2, List(AddressSet(0x2010000, 0xfff)), List(Resource(sifive.blocks.inclusivecache.InclusiveCache$$anon$1@5df06166,reg/control)), GET_EFFECTS, false, Some(0), ALGFP, PALGFPH, false, false, false),
// TLSlaveParameters(subsystem_pbus, List(AddressSet(0x4000, 0xfff)), List(Resource(freechips.rocketchip.diplomacy.SimpleDevice@54b9ec77,reg/control)),        GET_EFFECTS, false, Some(0), ALGFP, PALGFPH, false, false, false),
// TLSlaveParameters(frontend, List(AddressSet(0x10015000, 0xfff       )), List(Resource(freechips.rocketchip.tilelink.TLRegisterRouterBase$$anon$1@13f693d2,reg/control)), GET_EFFECTS, false, Some(0)       , ALGFP, PALGFPH, false, false, false),
// TLSlaveParameters(uart_0, List(AddressSet(0x54000000, 0xfff)), List(Resource(freechips.rocketchip.regmapper.RegisterRouter$$anon$1@5446e739,reg/control)), GET_EFFECTS, false, Some(0), ALGFP, PALGFPH, false, false, fa       lse),
// TLSlaveParameters(plic, List(AddressSet(0xc000000, 0x3ffffff)), List(Resource(freechips.rocketchip.devices.tilelink.TLPLIC       $$anon$2@ae34786,reg/control)), GET_EFFECTS, false, Some(0), ALGFP, PALGFPH, false, false, false), TLSlaveParameters(clint, List       (AddressSet(0x2000000, 0xffff)), List(Resource(freechips.rocketchip.devices.tilelink.CLINT$$anon$1@1122c8f9,reg/control)), GET_E       FFECTS, false, Some(0), ALGFP, PALGFPH, false, false, false), TLSlaveParameters(bootrom, List(AddressSet(0x10000, 0xffff)), List       (Resource(freechips.rocketchip.diplomacy.SimpleDevice@366da10c,reg/mem)), UNCACHED, true, Some(0), G, PALGFPH, false, false, fal       se), TLSlaveParameters(tileClockGater, List(AddressSet(0x100000, 0xfff)), List(Resource(freechips.rocketchip.diplomacy.SimpleDev       ice@5571f7d7,reg/control)), GET_EFFECTS, false, Some(0), ALGFP, PALGFPH, false, false, false), TLSlaveParameters(tileResetSetter       , List(AddressSet(0x110000, 0xfff)), List(Resource(freechips.rocketchip.diplomacy.SimpleDevice@45be5588,reg/control)), GET_EFFEC       TS, false, Some(0), ALGFP, PALGFPH, false, false, false), TLSlaveParameters(system, List(AddressSet(0x80000000, 0x7fffffff), Add       ressSet(0x100000000, 0xffffffff), AddressSet(0x200000000, 0x1ffffffff), AddressSet(0x400000000, 0x7fffffff)), List(Resource(sifi       ve.blocks.inclusivecache.InclusiveCache$$anon$1@5df06166,caches), Resource(freechips.rocketchip.diplomacy.MemoryDevice@42dc38c6,       reg)), CACHED, true, None, TBALGFPH, PALGFPH, false, true, true), TLSlaveParameters(serdesser, List(AddressSet(0x10000000, 0xfff       )), List(Resource(sifive.blocks.inclusivecache.InclusiveCache$$anon$1@5df06166,caches), Resource(freechips.rocketchip.diplomacy.       SimpleDevice@19e29450,reg)), CACHED, true, None, TBALGFPH, PALGFPH, false, false, false), TLSlaveParameters(serdesser, List(Addr       essSet(0x20000, 0xffff)), List(Resource(sifive.blocks.inclusivecache.InclusiveCache$$anon$1@5df06166,caches), Resource(freechips       .rocketchip.diplomacy.SimpleDevice@43f717f4,reg)), CACHED, true, None, BGH, PALGFPH, false, false, false)), TLChannelBeatBytes(S       ome(8),Some(8),Some(8),Some(8)), 8, 4, List(), List()),org.chipsalliance.cde.config$ChainParameters@74520817,SourceLine(RocketTi       le.scala,80,14))

case object IsFireChip extends Field[Boolean](false)
case object LatencyBetweenPartitions extends Field[Int](32)


case class DummyTileAttachParams(
  tileParams: DummyTileParams,
  crossingParams: RocketCrossingParams
) extends CanAttachTile {
  type TileType = DummyTile
// val lookup = PriorityMuxHartIdFromSeq(Seq(tileParams))
// val crossingParams = RocketCrossingParams()
}

case class DummyTileParams(
  core: RocketCoreParams = RocketCoreParams(),
  icache: Option[ICacheParams] = Some(ICacheParams()),
  dcache: Option[DCacheParams] = Some(DCacheParams()),
  btb: Option[BTBParams] = Some(BTBParams()),
  dataScratchpadBytes: Int = 0,
  name: Option[String] = Some("tile"),
  hartId: Int = 0,
  beuAddr: Option[BigInt] = None,
  blockerCtrlAddr: Option[BigInt] = None,
  clockSinkParams: ClockSinkParameters = ClockSinkParameters(),
  boundaryBuffers: Boolean = false // if synthesized with hierarchical PnR, cut feed-throughs?
) extends InstantiableTileParams[DummyTile] 
{
  def instantiate(crossing: TileCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters): DummyTile = {
    new DummyTile(this, crossing, lookup)
  }
}

class TileNodeWrapperModule(dummyParams: DummyTileParams, xBytes: Int, masterPortBeatBytes: Int)(implicit p: Parameters) extends LazyModule {


  def dummyDCacheMasterParams = Seq(TLMasterParameters.v1(
    name = "Dummy-DCache",
    sourceId = IdRange(0, 1),
    supportsProbe = TransferSizes(64, 64)))

  def dummyMMIOMasterParams = Seq(TLMasterParameters.v1(
    name = "Dummy-MMIO",
    sourceId = IdRange(1, 2),
    requestFifo = true))

  def dummyICacheMasterParams = Seq(TLMasterParameters.v1(
    name = "Dummy-ICache",
    sourceId = IdRange(2, 3)))

  val placeholderMasterNode = TLClientNode(
    Seq(TLMasterPortParameters.v1(
        clients = dummyDCacheMasterParams ++ dummyMMIOMasterParams ++ dummyICacheMasterParams
    )))


  override lazy val module = new TileNodeWrapperModuleImp(this)

  val masterPunchThroughIO = InModuleBody { placeholderMasterNode.makeIOs() }
}

class TileNodeWrapperModuleImp(outer: TileNodeWrapperModule) extends LazyModuleImp(outer) {
  dontTouch(outer.masterPunchThroughIO.head)
}



class DummyTile (val dummyParams: DummyTileParams,
                 crossing: ClockCrossingType,
                 lookup: LookupByHartIdImpl,
                 q: Parameters)
  extends BaseTile(dummyParams, crossing, lookup, q)
  with SinksExternalInterrupts
  with SourcesExternalNotifications
{

  def this(params: DummyTileParams, crossing: TileCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters) =
    this(params, crossing.crossingType, lookup, p)

  val intOutwardNode = IntIdentityNode()
  val slaveNode = TLIdentityNode()
  val masterNode = visibilityNode

  val nodeWrapper = LazyModule(new TileNodeWrapperModule(dummyParams, xBytes, masterPortBeatBytes))

//  intOutwardNode := nodeWrapper.bus_error_unit_intNode

  tlOtherMastersNode := nodeWrapper.placeholderMasterNode
  masterNode :=* tlOtherMastersNode
  DisableMonitors { implicit p => tlSlaveXbar.node :*= slaveNode }

  // Required entry of CPU device in the device tree for interrupt purpose
  val cpuDevice: SimpleDevice = new SimpleDevice("cpu", Seq("ucb-bar,dummy", "riscv")) {
    override def parent = Some(ResourceAnchors.cpus)

    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++
        cpuProperties ++
        nextLevelCacheProperty ++
        tileProperties)
    }
  }

  ResourceBinding {
    Resource(cpuDevice, "reg").bind(ResourceAddress(hartId))
  }

  override lazy val module = new DummyTileModuleImp(outer = this)

  InModuleBody {
    dontTouch(intSinkNode.in(0)._1)
  }
}

class DummyTileModuleImp(outer: DummyTile) extends BaseTileModuleImp(outer)
{
  val int_bundle = Wire(new TileInterrupts)
  outer.decodeCoreInterrupts(int_bundle)


  if (p(IsFireChip)) {
    val tile_bridge = Module(new TileBusSideBridge())

    tile_bridge.io.clock := clock
    tile_bridge.io.reset := reset.asBool

    tile_bridge.io.in.interrupts.in_3_0 := int_bundle.seip.get
    tile_bridge.io.in.interrupts.in_2_0 := int_bundle.meip
    tile_bridge.io.in.interrupts.in_1_1 := int_bundle.mtip
    tile_bridge.io.in.interrupts.in_1_0 := int_bundle.msip
    dontTouch(int_bundle)

    tile_bridge.io.in.hartid := outer.hartIdSinkNode.bundle
    dontTouch(outer.hartIdSinkNode.bundle)

    val tlmaster = outer.nodeWrapper.masterPunchThroughIO.head

    val latencyToEndure = p(LatencyBetweenPartitions)
    val tileAChannelSkidBuffer = Module(new SkidBuffer(data=DataMirror.internal.chiselTypeClone[TLBundleA](tlmaster.a.bits), latencyToEndure=latencyToEndure))
    val tileCChannelSkidBuffer = Module(new SkidBuffer(data=DataMirror.internal.chiselTypeClone[TLBundleC](tlmaster.c.bits), latencyToEndure=latencyToEndure))
    val tileEChannelSkidBuffer = Module(new SkidBuffer(data=DataMirror.internal.chiselTypeClone[TLBundleE](tlmaster.e.bits), latencyToEndure=latencyToEndure))

    tile_bridge.io.in.tlmaster_b_valid := tlmaster.b.valid
    tile_bridge.io.in.tlmaster_b_bits  := tlmaster.b.bits
    tlmaster.b.ready := tile_bridge.io.out.tlmaster_b_ready

    tile_bridge.io.in.tlmaster_d_valid := tlmaster.d.valid
    tile_bridge.io.in.tlmaster_d_bits  := tlmaster.d.bits
    tlmaster.d.ready := tile_bridge.io.out.tlmaster_d_ready


    tlmaster.a <> tileAChannelSkidBuffer.io.deq

    tileAChannelSkidBuffer.io.enq.valid := tile_bridge.io.out.tlmaster_a_valid
    tileAChannelSkidBuffer.io.enq.bits.opcode   := tile_bridge.io.out.tlmaster_a_bits.opcode
    tileAChannelSkidBuffer.io.enq.bits.param    := tile_bridge.io.out.tlmaster_a_bits.param
    tileAChannelSkidBuffer.io.enq.bits.size     := tile_bridge.io.out.tlmaster_a_bits.size
    tileAChannelSkidBuffer.io.enq.bits.source   := tile_bridge.io.out.tlmaster_a_bits.source
    tileAChannelSkidBuffer.io.enq.bits.address  := tile_bridge.io.out.tlmaster_a_bits.address
    tileAChannelSkidBuffer.io.enq.bits.mask     := tile_bridge.io.out.tlmaster_a_bits.mask
    tileAChannelSkidBuffer.io.enq.bits.data     := tile_bridge.io.out.tlmaster_a_bits.data
    tileAChannelSkidBuffer.io.enq.bits.corrupt  := false.B

    tile_bridge.io.in.tlmaster_a_ready  := tileAChannelSkidBuffer.io.readyPropagate
    assert(tileAChannelSkidBuffer.io.enq.ready === true.B, "tileAChannelSkidBuffer full")


    tlmaster.c <> tileCChannelSkidBuffer.io.deq

    tileCChannelSkidBuffer.io.enq.valid := tile_bridge.io.out.tlmaster_c_valid
    tileCChannelSkidBuffer.io.enq.bits.opcode  := tile_bridge.io.out.tlmaster_c_bits.opcode
    tileCChannelSkidBuffer.io.enq.bits.param   := tile_bridge.io.out.tlmaster_c_bits.param
    tileCChannelSkidBuffer.io.enq.bits.size    := tile_bridge.io.out.tlmaster_c_bits.size
    tileCChannelSkidBuffer.io.enq.bits.source  := tile_bridge.io.out.tlmaster_c_bits.source
    tileCChannelSkidBuffer.io.enq.bits.address := tile_bridge.io.out.tlmaster_c_bits.address
    tileCChannelSkidBuffer.io.enq.bits.data    := tile_bridge.io.out.tlmaster_c_bits.data
    tileCChannelSkidBuffer.io.enq.bits.corrupt := false.B

    tile_bridge.io.in.tlmaster_c_ready  := tileCChannelSkidBuffer.io.readyPropagate
    assert(tileCChannelSkidBuffer.io.enq.ready === true.B, "tileCChannelSkidBuffer full")


    tlmaster.e <> tileEChannelSkidBuffer.io.deq

    tileEChannelSkidBuffer.io.enq.valid := tile_bridge.io.out.tlmaster_e_valid
    tileEChannelSkidBuffer.io.enq.bits.sink := tile_bridge.io.out.tlmaster_e_bits.sink
    tile_bridge.io.in.tlmaster_e_ready := tileEChannelSkidBuffer.io.readyPropagate
    assert(tileEChannelSkidBuffer.io.enq.ready === true.B, "tileEChannelSkidBuffer full")


    when (tlmaster.a.fire) {
      Logger.printChannelA(tlmaster.a.bits)
    }

    when (tlmaster.b.fire) {
      Logger.printChannelB(tlmaster.b.bits)
    }

    when (tlmaster.c.fire) {
      Logger.printChannelC(tlmaster.c.bits)
    }

    when (tlmaster.d.fire) {
      Logger.printChannelD(tlmaster.d.bits)
    }

    when (tlmaster.e.fire) {
      Logger.printChannelE(tlmaster.e.bits)
    }
  } else {
    val bridge_emulator_blackbox = Module(new BridgeEmulatorBlackBox)
    bridge_emulator_blackbox.io.clock := clock
    bridge_emulator_blackbox.io.reset := reset.asBool

    val (wfi, _) = outer.wfiNode.out(0)
    wfi(0) := RegInit(0.U)

    dontTouch(wfi(0))
    dontTouch(outer.hartIdSinkNode.bundle)
  }
}

class TileBoundaryBundleIn extends Bundle {
  val debug = Input(Bool())
  val mtip = Input(Bool())
  val msip = Input(Bool())
  val meip = Input(Bool())
  val seip = Input(Bool())
  val hartid = Input(UInt(2.W))
  val tlmaster_a_ready = Input(Bool())
  val tlmaster_b_valid = Input(Bool())
  val tlmaster_b_bits_opcode = Input(UInt(3.W))
  val tlmaster_b_bits_param = Input(UInt(2.W))
  val tlmaster_b_bits_size = Input(UInt(4.W))
  val tlmaster_b_bits_source = Input(UInt(2.W))
  val tlmaster_b_bits_address = Input(UInt(32.W))
  val tlmaster_b_bits_data = Input(UInt(64.W))
  val tlmaster_b_bits_mask = Input(UInt(8.W))
  val tlmaster_b_bits_corrupt = Input(Bool())
  val tlmaster_c_ready = Input(Bool())
  val tlmaster_d_valid = Input(Bool())
  val tlmaster_d_bits_opcode = Input(UInt(3.W))
  val tlmaster_d_bits_param = Input(UInt(2.W))
  val tlmaster_d_bits_size = Input(UInt(4.W))
  val tlmaster_d_bits_source = Input(UInt(2.W))
  val tlmaster_d_bits_sink = Input(UInt(3.W))
  val tlmaster_d_bits_denied = Input(Bool())
  val tlmaster_d_bits_data = Input(UInt(64.W))
  val tlmaster_d_bits_corrupt = Input(Bool())
  val tlmaster_e_ready = Input(Bool())
}

class TileBoundaryBundleOut extends Bundle {
  val wfi = Output(Bool())
  val tlmaster_a_valid = Output(Bool())
  val tlmaster_a_bits_opcode = Output(UInt(3.W))
  val tlmaster_a_bits_param = Output(UInt(3.W))
  val tlmaster_a_bits_size = Output(UInt(4.W))
  val tlmaster_a_bits_source = Output(UInt(2.W))
  val tlmaster_a_bits_address = Output(UInt(32.W))
  val tlmaster_a_bits_mask = Output(UInt(8.W))
  val tlmaster_a_bits_data = Output(UInt(64.W))
  val tlmaster_a_bits_corrupt = Output(Bool())
  val tlmaster_b_ready = Output(Bool())
  val tlmaster_c_valid = Output(Bool())
  val tlmaster_c_bits_opcode = Output(UInt(3.W))
  val tlmaster_c_bits_param = Output(UInt(3.W))
  val tlmaster_c_bits_size = Output(UInt(4.W))
  val tlmaster_c_bits_source = Output(UInt(2.W))
  val tlmaster_c_bits_address = Output(UInt(32.W))
  val tlmaster_c_bits_data = Output(UInt(64.W))
  val tlmaster_c_bits_corrupt = Output(Bool())
  val tlmaster_d_ready = Output(Bool())
  val tlmaster_e_valid = Output(Bool())
  val tlmaster_e_bits_sink = Output(UInt(3.W))
}

class TileBoundaryBundle extends Bundle {
  val in  = new TileBoundaryBundleIn()
  val out = new TileBoundaryBundleOut()
}


class BridgeEmulatorBlackBox extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
  })

  addResource("/vsrc/BridgeEmulatorBlackBox.v")
}


///////////////////////////////////////////////////////////////////////////////



class WithDummyTile(n: Int = 1, tileParams: DummyTileParams = DummyTileParams(),
  overrideIdOffset: Option[Int] = None) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => {
    val prev = up(TilesLocated(InSubsystem), site)
    val idOffset = overrideIdOffset.getOrElse(prev.size)
    (0 until n).map { i =>
      DummyTileAttachParams(
        tileParams = tileParams.copy(
          hartId = i + idOffset,
          boundaryBuffers = true
        ),
        crossingParams = RocketCrossingParams()
      )
    } ++ prev
  }
})

class WithPartitionLatency(cycles: Int) extends Config((site, here, up) => {
  case LatencyBetweenPartitions => cycles
})

class DummyTileConfig extends Config(
  new chipyard.WithDummyTile ++
  new chipyard.config.AbstractConfig
)

class WithDummyTileFireSimBridges extends Config((site, here, up) => {
  case IsFireChip => true
})

class FireSimDummyTileConfig extends Config(
  new chipyard.WithPartitionLatency(32) ++
  new chipyard.WithDummyTileFireSimBridges ++
  new chipyard.WithDummyTile ++
  new chipyard.config.AbstractConfig
)



object Logger {
  def logInfo(format: String, args: Bits*)(implicit p: Parameters) {
    val loginfo_cycles = RegInit(0.U(64.W))
    loginfo_cycles := loginfo_cycles + 1.U

// if (p(IsFireChip)) {
// printf(midas.targetutils.SynthesizePrintf("cy: %d, ", loginfo_cycles))
// printf(midas.targetutils.SynthesizePrintf(format, args:_*))
// } else {
      printf("cy: %d, ", loginfo_cycles)
      printf(Printable.pack(format, args:_*))
// }
  }

  def printChannelA(a: TLBundleA)(implicit p: Parameters) {
    val a_echo = Cat(a.echo.fields.map(_.data.asUInt).toSeq)
    val a_user = Cat(a.user.fields.map(_.data.asUInt).toSeq)
    logInfo("[A] op %x param %x size %x source %x addr %x usr %x echo %x mask %x data %x cor %d\n",
      a.opcode,
      a.param,
      a.size,
      a.source,
      a.address,
      a_user,
      a_echo,
      a.mask,
      a.data,
      a.corrupt)
  }

  def printChannelD(d: TLBundleD)(implicit p: Parameters) {
    val d_echo = Cat(d.echo.fields.map(_.data.asUInt).toSeq)
    val d_user = Cat(d.user.fields.map(_.data.asUInt).toSeq)
    logInfo("[D] op %x param %x size %x source %x sink %x denied %d user %x echo %x data %x cor %d\n",
      d.opcode,
      d.param,
      d.size,
      d.source,
      d.sink,
      d.denied,
      d_user,
      d_echo,
      d.data,
      d.corrupt)
  }

  def printChannelB(b: TLBundleB)(implicit p: Parameters) {
    logInfo("[B] op %x param %x size %x source %x addr %x mask %x data %x cor %d\n",
      b.opcode,
      b.param,
      b.size,
      b.source,
      b.address,
      b.mask,
      b.data,
      b.corrupt)
  }


  def printChannelC(c: TLBundleC)(implicit p: Parameters) {
    val c_echo = Cat(c.echo.fields.map(_.data.asUInt).toSeq)
    val c_user = Cat(c.user.fields.map(_.data.asUInt).toSeq)
    logInfo("[C] op %x param %x size %x source %x addr %x user %x echo %x data %x cor %d\n",
      c.opcode,
      c.param,
      c.size,
      c.source,
      c.address,
      c_user,
      c_echo,
      c.data,
      c.corrupt)
  }

  def printChannelE(e: TLBundleE)(implicit p: Parameters) {
    logInfo("[E] sink %x\n",
      e.sink)
  }
}
