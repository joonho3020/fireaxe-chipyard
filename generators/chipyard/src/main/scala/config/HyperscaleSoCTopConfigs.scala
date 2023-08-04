package chipyard

import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tile._

class WithExtMemIdBits(n: Int) extends Config((site, here, up) => {
  case ExtMem => up(ExtMem, site).map(x => x.copy(master = x.master.copy(idBits = n)))
})

class WithRocketBoundaryBuffers extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
    case tp: RocketTileAttachParams => tp.copy(tileParams = tp.tileParams.copy(boundaryBuffers=Some(RocketTileBoundaryBufferParams(true))))
  }
})

//class WithBoomBoundaryBuffers extends Config((site, here, up) => {
//  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
//    case tp: boom.common.BoomTileAttachParams => tp.copy(tileParams = tp.tileParams.copy(boundaryBuffers=Some(RocketTileBoundaryBufferParams(true))))
//  }
//})



class HyperscaleSoCRocketBaseConfig extends Config(
  //new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=2048) ++
  //new freechips.rocketchip.subsystem.WithNBanks(8) ++
  new WithExtMemIdBits(7) ++
  //new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++
  //

  new Config ((site, here, up) => {
    case SystemBusKey => up(SystemBusKey, site).copy(beatBytes = 32)
  }) ++

  //==================================
  // Set up TestHarness
  //==================================
  new chipyard.harness.WithAbsoluteFreqHarnessClockInstantiator ++ // use absolute frequencies for simulations in the harness
                                                                   // NOTE: This only simulates properly in VCS
  new WithRocketBoundaryBuffers ++
  //==================================
  // Set up tiles
  //==================================
  new freechips.rocketchip.subsystem.WithAsynchronousRocketTiles(3, 3) ++    // Add rational crossings between RocketTile and uncore
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++                     // quad-core (4 RocketTiles)

  //==================================
  // Set up I/O
  //==================================
  new testchipip.WithSerialTLWidth(4) ++
  new testchipip.WithSerialTLBackingMemory ++                                           // Backing memory is over serial TL protocol
  new chipyard.harness.WithSimAXIMemOverSerialTL ++                                     // Attach fast SimDRAM to TestHarness
  new freechips.rocketchip.subsystem.WithExtMemSize((1 << 30) * 4L) ++                  // 4GB max external memory
  new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++                          // 1 memory channel

  //==================================
  // Set up clock./reset
  //==================================
  new chipyard.clocking.WithPLLSelectorDividerClockGenerator ++   // Use a PLL-based clock selector/divider generator structure

  // Create the uncore clock group
  new chipyard.clocking.WithClockGroupsCombinedByName(("uncore", Seq("implicit", "sbus", "mbus", "cbus", "system_bus", "fbus", "pbus"), Nil)) ++

  new chipyard.config.AbstractConfig)

class HyperscaleSoCRocketClientNodeConfig extends Config(
  new compressacc.AcceleratorPlacementRoCC ++
  new compressacc.WithSnappyCompleteASIC ++
  new protoacc.WithProtoAccelSerOnly ++
  new protoacc.WithProtoAccelDeserOnly ++
  new HyperscaleSoCRocketBaseConfig)

class HyperscaleSoCRocketServerNodeConfig extends Config(
  new compressacc.AcceleratorPlacementRoCC ++
  new compressacc.WithMergedCompressorLatencyInjection ++
  new compressacc.WithMergedDecompressor16Spec ++
  new protoacc.WithProtoAccelSerOnly ++
  new protoacc.WithProtoAccelDeserOnly ++
  new HyperscaleSoCRocketBaseConfig)


class HyperscaleSoCMegaBoomBaseConfig extends Config(
  //new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=2048) ++
  //new freechips.rocketchip.subsystem.WithNBanks(8) ++
  new WithExtMemIdBits(7) ++
  //new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++
  // this used to be up here: new boom.common.WithNMegaTapeoutBooms(1) ++

  new Config ((site, here, up) => {
    case SystemBusKey => up(SystemBusKey, site).copy(beatBytes = 32)
  }) ++

  //==================================
  // Set up TestHarness
  //==================================
  new chipyard.harness.WithAbsoluteFreqHarnessClockInstantiator ++ // use absolute frequencies for simulations in the harness
                                                                   // NOTE: This only simulates properly in VCS
  //new WithBoomBoundaryBuffers ++
  //==================================
  // Set up tiles
  //==================================
  new boom.common.WithAsynchronousBoomTiles ++    // Add rational crossings between RocketTile and uncore
  new boom.common.WithNMegaTapeoutBooms(1) ++

  //==================================
  // Set up I/O
  //==================================
  new testchipip.WithSerialTLWidth(4) ++
  new testchipip.WithSerialTLBackingMemory ++                                           // Backing memory is over serial TL protocol
  new chipyard.harness.WithSimAXIMemOverSerialTL ++                                     // Attach fast SimDRAM to TestHarness
  new freechips.rocketchip.subsystem.WithExtMemSize((1 << 30) * 4L) ++                  // 4GB max external memory
  new freechips.rocketchip.subsystem.WithNMemoryChannels(1) ++                          // 1 memory channel

  //==================================
  // Set up clock./reset
  //==================================
  new chipyard.clocking.WithPLLSelectorDividerClockGenerator ++   // Use a PLL-based clock selector/divider generator structure

  // Create the uncore clock group
  new chipyard.clocking.WithClockGroupsCombinedByName(("uncore", Seq("implicit", "sbus", "mbus", "cbus", "system_bus", "fbus", "pbus"), Nil)) ++

  new chipyard.config.AbstractConfig)

class HyperscaleSoCMegaBoomClientNodeConfig extends Config(
  new compressacc.AcceleratorPlacementRoCC ++
  new compressacc.WithSnappyCompleteASIC ++
  new protoacc.WithProtoAccelSerOnly ++
  new protoacc.WithProtoAccelDeserOnly ++
  new HyperscaleSoCMegaBoomBaseConfig)

class HyperscaleSoCMegaBoomServerNodeConfig extends Config(
  new compressacc.AcceleratorPlacementRoCC ++
  new compressacc.WithMergedCompressorLatencyInjection ++
  new compressacc.WithMergedDecompressor16Spec ++
  new protoacc.WithProtoAccelSerOnly ++
  new protoacc.WithProtoAccelDeserOnly ++
  new HyperscaleSoCMegaBoomBaseConfig)

class HyperscaleSoCRocketBaseConfig16MBL2 extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=16*1024) ++
  new HyperscaleSoCRocketBaseConfig)

class HyperscaleSoCRocketBaseConfig16MBL2And8MemChan extends Config(
  new freechips.rocketchip.subsystem.WithNMemoryChannels(8) ++
  new HyperscaleSoCRocketBaseConfig16MBL2)

class SnappyBothConfig16MBL2And8MemChanRoCC extends Config(
  new compressacc.AcceleratorPlacementRoCC ++
  new compressacc.WithSnappyCompleteFireSim ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class SnappyBothConfig16MBL2And8MemChanChiplet extends Config(
  new compressacc.AcceleratorPlacementChiplet ++
  new compressacc.WithSnappyCompleteFireSim ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class SnappyBothConfig16MBL2And8MemChanPCIeLocalCache extends Config(
  new compressacc.AcceleratorPlacementPCIeLocalCache ++
  new compressacc.WithSnappyCompleteFireSim ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class SnappyBothConfig16MBL2And8MemChanPCIeNoCache extends Config(
  new compressacc.AcceleratorPlacementPCIeNoCache ++
  new compressacc.WithSnappyCompleteFireSim ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class SnappyDecompressorConfig16MBL2And8MemChanPCIeNoCache extends Config(
  new compressacc.AcceleratorPlacementPCIeNoCache ++
  new compressacc.WithSnappyDecompressor ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class ZstdDecompressorConfig16MBL2And8MemChanSpec16 extends Config(
  new compressacc.WithZstdDecompressor16 ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class ZstdDecompressorConfig16MBL2And8MemChanSpec32 extends Config(
  new compressacc.WithZstdDecompressor32 ++
  new HyperscaleSoCRocketBaseConfig16MBL2And8MemChan)

class MergedCompressorLatencyInjectionHyperscaleRocketConfig8MBL2 extends Config(
  new freechips.rocketchip.subsystem.WithInclusiveCache(nWays=16, capacityKB=8*1024) ++
  new compressacc.WithMergedCompressorLatencyInjection ++
  new HyperscaleSoCRocketBaseConfig)
